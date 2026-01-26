"""
统一配置与通用函数
"""

import os
import random

import numpy as np
import pandas as pd
import torch
from torch.utils.data import Dataset
from transformers import BertTokenizer
from sklearn.model_selection import train_test_split


CONFIG = {
    # 模型与数据路径
    "BERT_MODEL_PATH": r"../bert-base-chinese",
    "DATA_DIR": "../waimai.csv",
    # 训练超参数（默认值会被实验预设覆盖）
    "EPOCHS": 5,
    "LEARNING_RATE": 3e-4,
    "BATCH_SIZE": 32,
    # 模型相关
    "MAX_LENGTH": 256,
    "DROPOUT": 0.5,
    "NUM_CLASSES": 2,
    # 数据切分
    "TRAIN_RATIO": 0.8,
    "VAL_RATIO": 0.1,
    "TEST_RATIO": 0.1,
    # LoRA 相关
    "LORA_R": 8,
    "LORA_ALPHA": 16,
    "LORA_DROPOUT": 0.1,
    "LORA_TARGET_MODULES": ["query", "value", "key", "dense"],
    # 优化器与冻结
    "WEIGHT_DECAY": 0.01,
    "FREEZE_BERT": False,
    # 随机种子与保存
    "RANDOM_SEED": 42,
    "SAVE_PATH_ALL": "./bert_all_checkpoint",
    "SAVE_PATH_LORA": "./bert_lora_checkpoint",
}

# ===============================
# 实验预设（学术对照建议）
# ===============================
# 使用说明：
# 1) 将 ACTIVE_EXPERIMENT 设为 "zero_shot" / "full_ft" / "lora_ft"
# 2) 下面的 apply_experiment_config 会自动覆盖 CONFIG 中相关字段
# 3) 训练/验证流程不变，仅改变配置
ACTIVE_EXPERIMENT = "full_ft"

EXPERIMENT_PRESETS = {
    # Zero-shot baseline：冻结BERT，仅训练分类头
    "zero_shot": {
        "FREEZE_BERT": True,
        "LEARNING_RATE": 3e-4,
        "EPOCHS": 3,
        "DROPOUT": 0.5,
    },
    # Full fine-tuning：全参数微调
    "full_ft": {
        "FREEZE_BERT": False,
        "LEARNING_RATE": 3e-5,
        "EPOCHS": 3,
        "DROPOUT": 0.1,
    },
    # LoRA fine-tuning：低秩适配
    "lora_ft": {
        "FREEZE_BERT": False,
        "LEARNING_RATE": 3e-4,
        "EPOCHS": 5,
        "DROPOUT": 0.5,
    },
}


def apply_experiment_config(config: dict, experiment: str) -> None:
    preset = EXPERIMENT_PRESETS.get(experiment)
    if preset is None:
        raise ValueError(
            f"未知实验类型: {experiment}，请使用 "
            f"{', '.join(EXPERIMENT_PRESETS.keys())}"
        )
    config.update(preset)


apply_experiment_config(CONFIG, ACTIVE_EXPERIMENT)


def get_save_path(exp_type: str) -> str:
    if exp_type.lower() == "all":
        return CONFIG["SAVE_PATH_ALL"]
    if exp_type.lower() == "lora":
        return CONFIG["SAVE_PATH_LORA"]
    raise ValueError("exp_type 仅支持 all 或 lora")


def setup_seed(seed: int) -> None:
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    np.random.seed(seed)
    random.seed(seed)
    torch.backends.cudnn.deterministic = True


def build_tokenizer(model_path: str) -> BertTokenizer:
    return BertTokenizer.from_pretrained(model_path)


class TextDataset(Dataset):
    def __init__(self, df: pd.DataFrame, tokenizer: BertTokenizer, max_length: int):
        self.labels = df["label"].astype(int).values
        self.texts = [
            tokenizer(
                text,
                padding="max_length",
                max_length=max_length,
                truncation=True,
                return_tensors="pt",
            )
            for text in df["review"]
        ]

    def __len__(self) -> int:
        return len(self.labels)

    def __getitem__(self, idx):
        return self.texts[idx], torch.tensor(self.labels[idx], dtype=torch.long)


def generate_data(
    mode: str,
    data_path: str,
    tokenizer: BertTokenizer,
    max_length: int,
    seed: int,
):
    df = pd.read_csv(data_path)

    if mode == "train":
        print("=" * 50)
        print("数据基本信息")
        print("=" * 50)
        print(f"数据总量: {len(df)}")
        print("\n标签分布:")
        print(df["label"].value_counts())
        print("\n数据示例（前3条）:")
        print(df.head(3))

    train_df, temp_df = train_test_split(
        df, test_size=0.3, stratify=df["label"], random_state=seed
    )
    val_df, test_df = train_test_split(
        temp_df, test_size=0.5, stratify=temp_df["label"], random_state=seed
    )

    if mode == "train":
        print("\n" + "=" * 50)
        print("数据分割结果")
        print("=" * 50)
        print(f"训练集大小: {len(train_df)} ({len(train_df)/len(df)*100:.1f}%)")
        print(f"验证集大小: {len(val_df)} ({len(val_df)/len(df)*100:.1f}%)")
        print(f"测试集大小: {len(test_df)} ({len(test_df)/len(df)*100:.1f}%)")
        print("\n✅ 数据加载和预处理完成")
        return TextDataset(train_df, tokenizer, max_length)
    if mode == "val":
        return TextDataset(val_df, tokenizer, max_length)
    if mode == "test":
        return TextDataset(test_df, tokenizer, max_length)
    raise ValueError("不支持的模式，请使用 train / val / test")
