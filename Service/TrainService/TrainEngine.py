"""
训练引擎：按请求配置在基座 BERT 上做 LoRA 微调，支持 ifOverTrain/baseModelVersion，
按 batch 更新 Redis 快照，保存最优模型到 lora_url/domainUrl/v-{modelVersion}，并回调训练结果。
"""
import os
import time
import asyncio
from datetime import datetime
from typing import List, Tuple, Callable

import pandas as pd
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_recall_fscore_support
from transformers import (
    BertTokenizer,
    BertForSequenceClassification,
    BertConfig,
    get_linear_schedule_with_warmup,
)
from torch.optim import AdamW
from peft import LoraConfig, get_peft_model, PeftModel, TaskType

from Config import get_settings
from Service.Redis.RedisService import update_train_task
from Dto.Redis.TrainTaskSnapshot import TrainTaskSnapshot
from Dto.receive.TrainDataRec import TrainDataRes
from Dto.send.TrainDataSend import TrainDataSend
from Common.SendBody import SendBody
from Common.Https import post_springboot
from Service.TrainService.Bert_Config import setup_seed


class _TextDataset(Dataset):
    """
    内部数据集：将 (content, label) 列表预处理为 BERT 所需的 tokenized inputs 与 label tensor。
    在构造时完成全量 tokenization，避免 DataLoader 迭代时重复计算。
    """

    def __init__(self, texts: List[str], labels: List[int], tokenizer: BertTokenizer, max_length: int):
        self.labels = [int(l) for l in labels]
        # 构造时完成全量分词，padding 到 max_length，返回 pt tensor
        self.texts = [
            tokenizer(
                t,
                padding="max_length",
                max_length=max_length,
                truncation=True,
                return_tensors="pt",
            )
            for t in texts
        ]

    def __len__(self) -> int:
        return len(self.labels)

    def __getitem__(self, idx):
        return self.texts[idx], torch.tensor(self.labels[idx], dtype=torch.long)

# 在黄金验证集中验证训练模型
def _evaluate_bert(model, dataloader, criterion, device) -> Tuple[float, float, float, float, float]:
    """
    在指定 dataloader 上评估模型，返回 (acc, precision, recall, f1, avg_loss)。
    评估期间关闭梯度计算与 dropout。
    """
    model.eval()
    total_loss = 0.0
    all_labels = []
    all_preds = []

    with torch.no_grad():
        for inputs, labels in dataloader:
            labels = labels.to(device)
            mask = inputs["attention_mask"].to(device)
            input_ids = inputs["input_ids"].squeeze(1).to(device)

            outputs = model(input_ids=input_ids, attention_mask=mask, labels=labels)
            logits = outputs.logits

            # 使用 criterion 计算 loss，乘以批次大小再累加，最后除以总样本数得平均 loss
            loss = criterion(logits, labels)
            total_loss += loss.item() * labels.size(0)

            preds = logits.argmax(dim=1)
            all_labels.extend(labels.cpu().numpy().tolist())
            all_preds.extend(preds.cpu().numpy().tolist())

    n = len(dataloader.dataset)
    avg_loss = total_loss / n if n else 0.0
    acc = accuracy_score(all_labels, all_preds) if all_labels else 0.0
    # 二分类指标，zero_division=0 避免因预测全为同一类时报错
    precision, recall, f1, _ = precision_recall_fscore_support(
        all_labels, all_preds, average="binary", zero_division=0
    )
    return acc, precision, recall, f1, avg_loss


class TrainEngine:
    """
    训练引擎。
    - __init__ 在服务启动时加载 tokenizer，避免每次请求重复加载。
    - _run_training 为同步阻塞方法，在 to_thread 里执行，完成完整训练流程并返回结果 DTO。
    - background_train_task 为异步入口，由 FastAPI BackgroundTasks 调度。
    """

    def __init__(self):
        settings = get_settings()
        self.settings = settings
        # 优先使用 GPU，否则降级到 CPU
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        # tokenizer 随引擎启动时加载，所有训练任务复用同一实例
        self.tokenizer = BertTokenizer.from_pretrained(settings.bert_model_path)

    def _gold_test_path(self, domain_url: str) -> str:
        """
        根据 domainUrl 拼接 GoldTest 测试集文件路径。
        以本文件所在目录上溯两级（即项目根）作为基准。
        """
        base = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        return os.path.join(base, "GoldTest", f"{domain_url}.csv")

    def _run_training(
            self,
            request: TrainDataRes,
            progress_callback: Callable[[TrainTaskSnapshot], None],
    ) -> TrainDataSend:
        """
        同步训练主流程（运行在独立线程内，不阻塞事件循环）。
        完成数据准备、模型构建、训练循环、测试集评估，返回 TrainDataSend 结果 DTO。

        progress_callback：每个 batch 结束后调用，将快照发布到 Redis。
        """
        settings = self.settings

        # ---- 从请求中解包训练参数 ----
        task_id = request.taskId
        domain_url = request.domainUrl
        model_version = request.modelVersion
        base_version = request.baseModelVersion  # ifOverTrain=False 时加载的基座 LoRA 版本
        epochs = request.epochs
        batch_size = request.batchSize
        lr = float(request.learningRate)
        seed = request.randomSeed
        train_ratio = float(request.trainSplitRatio)
        if_over_train = request.ifOverTrain
        lora_r = request.loraR
        lora_alpha = request.loraAlpha
        lora_modules = request.loraModules
        max_length = settings.max_length
        num_classes = settings.num_classes
        dropout = settings.dropout
        weight_decay = settings.weight_decay

        # 固定全局随机种子，保证每次训练结果可复现
        setup_seed(seed)

        # ---- 数据准备：混合所有来源，不区分 source ----
        all_items: List[Tuple[int, str, int]] = []
        for group in request.trainDataList:
            for item in group.trainDataList:
                all_items.append((item.id, item.content, item.label))
        if not all_items:
            raise ValueError("训练数据列表为空")

        df = pd.DataFrame(all_items, columns=["id", "content", "label"])
        ids = df["id"].tolist()

        # 按 trainSplitRatio 分层切分为训练集与验证集，用于训练循环和每轮 val 指标
        train_df, val_df = train_test_split(
            df, train_size=train_ratio, stratify=df["label"], random_state=seed
        )
        train_contents = train_df["content"].tolist()
        train_labels = train_df["label"].tolist()
        val_contents = val_df["content"].tolist()
        val_labels = val_df["label"].tolist()

        # 构建 PyTorch Dataset 与 DataLoader
        train_dataset = _TextDataset(train_contents, train_labels, self.tokenizer, max_length)
        val_dataset = _TextDataset(val_contents, val_labels, self.tokenizer, max_length)
        train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
        val_loader = DataLoader(val_dataset, batch_size=batch_size)
        epoch_total_batches = len(train_loader)

        # ---- GoldTest 测试集：用于训练结束后计算最终测试指标（accuracy/precision/recall/f1）----
        gold_path = self._gold_test_path(domain_url)
        if not os.path.exists(gold_path):
            raise FileNotFoundError(f"GoldTest 测试集不存在: {gold_path}")
        gold_df = pd.read_csv(gold_path)
        if "data" not in gold_df.columns or "label" not in gold_df.columns:
            raise ValueError(f"GoldTest 文件需包含 data 与 label 两列: {gold_path}")
        test_dataset = _TextDataset(
            gold_df["data"].astype(str).tolist(),
            gold_df["label"].astype(int).tolist(),
            self.tokenizer,
            max_length,
        )
        test_loader = DataLoader(test_dataset, batch_size=batch_size)

        # ---- 基座 BERT 模型加载 ----
        config = BertConfig.from_pretrained(settings.bert_model_path)
        config.hidden_dropout_prob = dropout
        config.attention_probs_dropout_prob = dropout
        config.num_labels = num_classes
        base_model = BertForSequenceClassification.from_pretrained(
            settings.bert_model_path,
            config=config,
        ).to(self.device)

        # ---- LoRA 装配：ifOverTrain 决定是全新训练还是在已有 LoRA 上继续训练 ----
        if if_over_train:
            # 全新训练：在基座 BERT 上附加新的 LoRA 适配器
            lora_config = LoraConfig(
                task_type=TaskType.SEQ_CLS,
                r=lora_r,
                lora_alpha=lora_alpha,
                lora_dropout=0.1,
                target_modules=lora_modules,
                bias="none",
                modules_to_save=["classifier"],
                inference_mode=False,
            )
            model = get_peft_model(base_model, lora_config)
        else:
            # 继续训练：加载 baseModelVersion 对应的已有 LoRA 参数，在其基础上继续微调
            lora_base_path = os.path.join(settings.lora_url, domain_url, f"v-{base_version}")
            if not os.path.exists(lora_base_path):
                raise FileNotFoundError(f"基座 LoRA 未找到: {lora_base_path}")
            model = PeftModel.from_pretrained(base_model, lora_base_path)

        model = model.to(self.device)

        # ---- 优化器与学习率调度器 ----
        criterion = nn.CrossEntropyLoss().to(self.device)
        optimizer = AdamW(model.parameters(), lr=lr, weight_decay=weight_decay, eps=1e-8)
        total_steps = len(train_loader) * epochs
        # 前 10% 步数做 warmup，之后线性衰减学习率
        warmup_steps = int(total_steps * 0.1)
        scheduler = get_linear_schedule_with_warmup(
            optimizer, num_warmup_steps=warmup_steps, num_training_steps=total_steps
        )

        # 最优模型保存路径（按验证集准确率覆盖保存，只保留一份最优权重）
        save_dir = os.path.join(settings.lora_url, domain_url, f"v-{model_version}")
        os.makedirs(save_dir, exist_ok=True)

        # 各轮指标累积列表，对应 TrainDataSend 的 18-21 字段
        train_loss_list: List[float] = []
        train_acc_list: List[float] = []
        val_loss_list: List[float] = []
        val_acc_list: List[float] = []
        best_val_acc = 0.0
        current_val_acc = 0.0
        current_val_precision = 0.0
        current_val_recall = 0.0
        current_val_f1 = 0.0
        start_time = time.time()
        total_batches = epochs * epoch_total_batches  # 用于计算全局进度百分比

        # ---- 训练循环：每轮完整执行，不做早停 ----
        for epoch in range(epochs):
            model.train()
            total_acc_train = 0.0
            total_loss_train = 0.0

            for batch_idx, (train_input, train_label) in enumerate(train_loader):
                train_label = train_label.to(self.device)
                mask = train_input["attention_mask"].to(self.device)
                input_id = train_input["input_ids"].squeeze(1).to(self.device)

                # 前向传播，LoRA 模型直接返回 loss
                outputs = model(input_ids=input_id, attention_mask=mask, labels=train_label)
                batch_loss = outputs.loss
                logits = outputs.logits

                # 累加加权 loss 与正确预测数（用于本轮末汇总平均值）
                total_loss_train += batch_loss.item() * train_label.size(0)
                acc = (logits.argmax(dim=1) == train_label).sum().item()
                total_acc_train += acc

                # 反向传播 + 梯度裁剪（防止梯度爆炸）+ 参数更新
                model.zero_grad()
                batch_loss.backward()
                torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
                optimizer.step()
                scheduler.step()

                # ---- 每 batch 完成后更新进度快照（与 tqdm 时机一致）----
                duration = time.time() - start_time
                current_batch = batch_idx + 1
                processed = epoch * epoch_total_batches + current_batch
                progress_pct = 100.0 * processed / total_batches
                snap = TrainTaskSnapshot(
                    taskId=task_id,
                    duration=duration,
                    status=0,
                    statusMsg=f"Epoch {epoch + 1}/{epochs}, Batch {current_batch}/{epoch_total_batches}",
                    currentEpoch=epoch + 1,
                    totalEpochs=epochs,
                    currentBatch=current_batch,
                    epochTotalBatches=epoch_total_batches,
                    progressPercent=progress_pct,
                    accuracy=current_val_acc,
                    precisionRate=current_val_precision,
                    recallRate=current_val_recall,
                    f1Score=current_val_f1,
                )
                progress_callback(snap)

            # ---- 每轮结束：计算训练集和验证集指标并追加到列表 ----
            train_loss_avg = total_loss_train / len(train_dataset)
            train_acc_avg = total_acc_train / len(train_dataset)
            val_acc, val_precision, val_recall, val_f1, val_loss = _evaluate_bert(
                model, val_loader, criterion, self.device
            )
            current_val_acc = round(val_acc, 4)
            current_val_precision = round(val_precision, 4)
            current_val_recall = round(val_recall, 4)
            current_val_f1 = round(val_f1, 4)
            train_loss_list.append(round(train_loss_avg, 4))
            train_acc_list.append(round(train_acc_avg, 4))
            val_loss_list.append(round(val_loss, 4))
            val_acc_list.append(round(val_acc, 4))

            # 若验证准确率超过历史最优，则覆盖保存当前 LoRA 权重
            if val_acc > best_val_acc:
                best_val_acc = val_acc
                model.save_pretrained(save_dir)
        # 总体训练结束
        print("**********************训练结束")
        # ---- 训练结束：在 GoldTest 测试集上评估，得到最终测试指标（14-17 字段）----
        duration = time.time() - start_time
        test_acc, test_precision, test_recall, test_f1, _ = _evaluate_bert(
            model, test_loader, criterion, self.device
        )
        end_time_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print("**********************训练返回")
        return TrainDataSend(
            taskId=task_id,
            domainId=request.domainId,
            modelVersion=model_version,
            processStatus=1,
            endTime=end_time_str,
            duration=round(duration, 2),
            results=ids,  # 全部训练样本 id 列表
            accuracy=round(test_acc, 4),  # GoldTest 测试集准确率
            precisionRate=round(test_precision, 4),  # GoldTest 测试集精确率
            recallRate=round(test_recall, 4),  # GoldTest 测试集召回率
            f1Score=round(test_f1, 4),  # GoldTest 测试集 F1
            trainLossList=train_loss_list,  # 各轮训练集 loss
            trainAccList=train_acc_list,  # 各轮训练集 acc
            valLossList=val_loss_list,  # 各轮验证集 loss
            valAccList=val_acc_list,  # 各轮验证集 acc
            epochTotalBatches=epoch_total_batches,
        )

    async def background_train_task(self, request: TrainDataRes) -> None:
        """
        异步后台入口，由 FastAPI BackgroundTasks 调度。
        将阻塞的 _run_training 丢进线程池执行，避免阻塞事件循环。
        训练完成或失败后均回调 SpringBoot 并更新 Redis 终态快照。
        """
        settings = get_settings()
        back_url = settings.springboot_base_url + settings.train_res_url
        task_id = request.taskId
        loop = asyncio.get_running_loop()

        def progress_callback(snap: TrainTaskSnapshot) -> None:
            # 训练线程通过 call_soon_threadsafe 将协程调度回事件循环执行
            loop.call_soon_threadsafe(
                lambda: asyncio.create_task(update_train_task(snap))
            )

        # 写入初始快照：任务启动，进度 0%
        await update_train_task(
            TrainTaskSnapshot(
                taskId=task_id,
                duration=0.0,
                status=0,
                statusMsg="ready",
                currentEpoch=0,
                totalEpochs=request.epochs,
                currentBatch=0,
                epochTotalBatches=0,
                progressPercent=0.0,
            )
        )
        try:
            # 在独立线程中执行同步训练主流程，不阻塞 FastAPI 事件循环
            train_result = await asyncio.to_thread(
                self._run_training,
                request,
                progress_callback,
            )
            print("************debug**********进入1")
            end_time_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            # 训练成功：回调 SpringBoot 并写入完成快照
            await update_train_task(
                TrainTaskSnapshot(
                    taskId=task_id,
                    duration=train_result.duration,
                    status=1,
                    statusMsg="已完成",
                    currentEpoch=request.epochs,
                    totalEpochs=request.epochs,
                    currentBatch=train_result.epochTotalBatches,
                    epochTotalBatches=train_result.epochTotalBatches,
                    progressPercent=100.0,
                    accuracy=train_result.accuracy,
                    precisionRate=train_result.precisionRate,
                    recallRate=train_result.recallRate,
                    f1Score=train_result.f1Score,
                ),
                currentTime=end_time_str,
            )
            await post_springboot(SendBody.success(data=train_result), back_url)
        except Exception as e:
            error_msg = str(e)
            end_time_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            # 训练失败：构造默认失败结果体，回调 SpringBoot 并写入异常快照
            fail_data = TrainDataSend(
                taskId=task_id,
                domainId=request.domainId,
                modelVersion=request.modelVersion,
                processStatus=2,
                endTime=end_time_str,
                duration=0.0,
                results=[],
                accuracy=0.0,
                precisionRate=0.0,
                recallRate=0.0,
                f1Score=0.0,
                trainLossList=[],
                trainAccList=[],
                valLossList=[],
                valAccList=[],
            )
            await update_train_task(
                TrainTaskSnapshot(
                    taskId=task_id,
                    duration=0.0,
                    status=2,
                    statusMsg=error_msg,
                )
            )
            await post_springboot(SendBody.fail(message=error_msg, code=500, data=fail_data), back_url)
