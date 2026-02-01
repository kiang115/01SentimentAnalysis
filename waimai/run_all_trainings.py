"""
一键按顺序执行所有训练 notebook，并将每个 notebook 最后一个 cell 的输出收集到 result.txt。
不修改任何原有 notebook 文件。

运行方法
cd waimai
python run_all_trainings.py
"""

import os
import traceback

import nbformat
from nbconvert.preprocessors import ExecutePreprocessor


NOTEBOOKS = [
    "ML_Funning.ipynb",
    "RF_Funning.ipynb",
    "SVM_Funning.ipynb",
    "LSTM_Funning.ipynb",
    "TextCNN_Funning.ipynb",
    "NO_Funning.ipynb",
    "ALL_Funning.ipynb",
    "LORA_Funning.ipynb",
]

SEEDS = [40, 41, 42, 43, 44]
NOTEBOOKS_MULTI_SEED = [
    "ALL_Funning.ipynb",
    "LORA_Funning.ipynb",
    "LSTM_Funning.ipynb",
    "ML_Funning.ipynb",
    "NO_Funning.ipynb",
    "RF_Funning.ipynb",
    "SVM_Funning.ipynb",
    "TextCNN_Funning.ipynb",
]

RESULT_FILE = "result.txt"
CELL_TIMEOUT = 3600  # 每个 cell 最长执行时间（秒）


def get_script_dir():
    """获取脚本所在目录的绝对路径"""
    return os.path.dirname(os.path.abspath(__file__))


def extract_last_cell_output(nb):
    """从执行后的 notebook 中提取最后一个代码 cell 的 stdout 输出"""
    code_cells = [c for c in nb.cells if c.cell_type == "code"]
    if not code_cells:
        return "(无代码 cell)"
    last_cell = code_cells[-1]
    texts = []
    for out in last_cell.outputs:
        if getattr(out, "output_type", None) == "stream" and getattr(out, "name", None) == "stdout":
            texts.append(out.text)
    return "".join(texts) if texts else "(无输出)"


def main():
    script_dir = get_script_dir()
    os.chdir(script_dir)

    with open(RESULT_FILE, "w", encoding="utf-8") as out_f:
        for nb_path in NOTEBOOKS:
            runs = [(None,)] if nb_path not in NOTEBOOKS_MULTI_SEED else [(s,) for s in SEEDS]
            for run_arg in runs:
                seed = run_arg[0]
                if seed is not None:
                    os.environ["NOTEBOOK_SEED"] = str(seed)
                    header = f"========== {nb_path} (seed={seed}) =========="
                else:
                    header = f"========== {nb_path} =========="
                try:
                    print(f"正在执行: {header} ...")
                    nb = nbformat.read(nb_path, as_version=4)
                    ep = ExecutePreprocessor(timeout=CELL_TIMEOUT)
                    ep.preprocess(nb, {"metadata": {"path": script_dir}})
                    text = extract_last_cell_output(nb)
                    out_f.write(f"{header}\n\n{text}\n\n")
                    out_f.flush()
                    print(f"  完成，结果已写入 {RESULT_FILE}")
                except Exception as e:
                    err_msg = f"错误: {e}\n\n{traceback.format_exc()}"
                    out_f.write(f"{header}\n\n{err_msg}\n\n")
                    out_f.flush()
                    print(f"  执行失败: {e}")
                finally:
                    os.environ.pop("NOTEBOOK_SEED", None)

    print(f"\n全部完成，结果已保存到 {os.path.join(script_dir, RESULT_FILE)}")


if __name__ == "__main__":
    main()
