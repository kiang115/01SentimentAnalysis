from datetime import datetime

import torch
import gc
import os

from transformers import BertTokenizer, BertForSequenceClassification
from peft import PeftModel
from Service.Redis.RedisService import update_task_redis
from Dto.Redis.InferTaskSnapshot import InferTaskSnapshot
import asyncio
import time
from Dto.send.InferDataSend import CommentResult, InferenceDataResponse
from Common.SendBody import SendBody
from Dto.receive.InferDataReceive import InferenceRequest
from Common.Https import post_springboot
from Config import get_settings


class InferenceEngine:
    def __init__(self):
        settings = get_settings()
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.micro_batch_size = settings.max_gpu_batch_size

        # 使用 settings 替换 CONFIG
        self.tokenizer = BertTokenizer.from_pretrained(settings.bert_model_path)

        # 基础模型常驻显存
        self.base_model = BertForSequenceClassification.from_pretrained(
            settings.bert_model_path,
            num_labels=settings.num_classes
        ).to(self.device).eval()

    def predict_domain_batch(self, domain_url: str, version: str, comments_data: list, modelId: int, domainId: int,
                             on_batch_complete=None):
        settings = get_settings()
        # 使用 settings.lora_url 替换 CONFIG["LORA_URL"]
        lora_path = os.path.join(settings.lora_url, domain_url, f"v-{version}")

        if not os.path.exists(lora_path):
            raise FileNotFoundError(f"LoRA模型未找到: {lora_path}")

        model = PeftModel.from_pretrained(self.base_model, lora_path).to(self.device).eval()
        results = []
        contents = [c['content'] for c in comments_data]
        comment_ids = [c['commentId'] for c in comments_data]

        try:
            with torch.no_grad():
                for i in range(0, len(contents), self.micro_batch_size):
                    batch_contents = contents[i: i + self.micro_batch_size]
                    batch_ids = comment_ids[i: i + self.micro_batch_size]

                    inputs = self.tokenizer(
                        batch_contents,
                        return_tensors="pt",
                        padding=True,
                        truncation=True,
                        max_length=settings.max_length  # 替换 CONFIG["MAX_LENGTH"]
                    ).to(self.device)

                    outputs = model(**inputs)
                    probs_all = torch.softmax(outputs.logits, dim=1).cpu().numpy()

                    batch_results = []
                    for j, probs in enumerate(probs_all):
                        neg_p, pos_p = round(float(probs[0]), 4), round(float(probs[1]), 4)
                        sentiment = int(probs.argmax())

                        res = CommentResult(
                            commentId=batch_ids[j],
                            modelSentiment=sentiment,
                            positiveProb=pos_p,
                            negativeProb=neg_p,
                            confidence=pos_p if sentiment == 1 else neg_p,
                            modelId=modelId,
                            domainId=domainId,
                        )
                        batch_results.append(res)

                    results.extend(batch_results)

                    if on_batch_complete:
                        on_batch_complete(len(batch_contents))
            return results
        finally:
            del model
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
            gc.collect()

    async def background_inference_task(self, request: InferenceRequest):
        settings = get_settings()
        all_results = []

        # 使用 settings 替换 FAST_CONFIG
        back_url = settings.springboot_base_url + settings.inference_res_url

        processed_count = 0
        start_time = time.time()
        task_id = request.taskId
        loop = asyncio.get_running_loop()

        def on_batch_done(batch_size_count: int):
            nonlocal processed_count
            processed_count += batch_size_count
            current_duration = time.time() - start_time
            batch_snapshot = InferTaskSnapshot(task_id, processed_count, current_duration, 0, "处理中")
            loop.call_soon_threadsafe(
                lambda: asyncio.create_task(
                    update_task_redis(batch_snapshot)
                )
            )

        try:
            await update_task_redis(InferTaskSnapshot(task_id, 0, 0, 0, "处理中"))
            for domain_data in request.inferenceDomainDataList:
                res = await asyncio.to_thread(
                    self.predict_domain_batch,
                    domain_url=domain_data.domainUrl,
                    version = domain_data.modelVersion,
                    comments_data=[c.model_dump() for c in domain_data.inferenceDomainCommentList],
                    on_batch_complete=on_batch_done,
                    modelId=domain_data.modelId,
                    domainId=domain_data.domainId
                )
                all_results.extend(res)

            duration = time.time() - start_time
            currentTime = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            final_comment_result = InferenceDataResponse(
                taskId=task_id,
                processStatus=1,  # 完成
                results=all_results,
                processCount=processed_count,
                taskEndTime=currentTime,
                taskDuration=duration
            )
            success_body = SendBody.success(data=final_comment_result)
            await post_springboot(success_body, back_url)
            await update_task_redis(
                InferTaskSnapshot(task_id, processed_count, duration, 1, "已完成"), currentTime)
        except Exception as e:
            error_msg = f"任务失败: {str(e)}"
            print(error_msg)
            all_raw_results=[]
            for domain_data in request.inferenceDomainDataList:
                current_model_id = domain_data.modelId
                current_domain_id = domain_data.domainId

                # 2. 遍历该 domain 下的所有评论
                for comment in domain_data.inferenceDomainCommentList:
                    # 3. 创建结果对象并赋值
                    result = CommentResult(
                        commentId=comment.commentId,
                        modelId=current_model_id,
                        domainId=current_domain_id
                        # 其他字段使用默认值
                    )
                    all_raw_results.append(result)

            # 4. 组装响应对象
            fail_response = InferenceDataResponse(
                taskId=request.taskId,
                processStatus=2,
                processCount=len(all_results),
                results=all_raw_results
            )
            fail_body = SendBody.fail(message=error_msg, code=500, data=fail_response)
            await post_springboot(fail_body, back_url)
            await update_task_redis(
                InferTaskSnapshot(task_id, processed_count, time.time() - start_time, 2, error_msg))
