import torch
import gc
import os
from transformers import BertTokenizer, BertForSequenceClassification
from peft import PeftModel
from Bert_Config import CONFIG
from HostService.HostService import BusinessException, ResultBody


class InferenceEngine:
    def __init__(self):
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.micro_batch_size = CONFIG["MAX_GPU_BATCH_SIZE"]
        self.tokenizer = BertTokenizer.from_pretrained(CONFIG["BERT_MODEL_PATH"])
        # 基础模型常驻显存
        self.base_model = BertForSequenceClassification.from_pretrained(
            CONFIG["BERT_MODEL_PATH"],
            num_labels=CONFIG["NUM_CLASSES"]
        ).to(self.device).eval()

    # Inference_Engine.py 内部
    def predict_domain_batch(self, domain_url: str, version: str, comments_data: list, on_batch_complete=None):
        lora_path = os.path.join(CONFIG["LORA_URL"], domain_url, f"v-{version}")
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
                        batch_contents, return_tensors="pt", padding=True,
                        truncation=True, max_length=CONFIG["MAX_LENGTH"]
                    ).to(self.device)

                    outputs = model(**inputs)
                    probs_all = torch.softmax(outputs.logits, dim=1).cpu().numpy()

                    batch_results = []
                    for j, probs in enumerate(probs_all):
                        neg_p, pos_p = round(float(probs[0]), 4), round(float(probs[1]), 4)
                        sentiment = int(probs.argmax())
                        res = {
                            "commentId": batch_ids[j],
                            "modelSentiment": sentiment,
                            "positiveProb": pos_p,
                            "negativeProb": neg_p,
                            "confidence": pos_p if sentiment == 1 else neg_p
                        }
                        batch_results.append(res)

                    results.extend(batch_results)

                    # --- 新增：每完成一个 batch，执行一次回调 ---
                    if on_batch_complete:
                        # 传入当前这批处理的数量
                        on_batch_complete(len(batch_contents))
            return results
        finally:
            del model
            if torch.cuda.is_available(): torch.cuda.empty_cache()
            gc.collect()
