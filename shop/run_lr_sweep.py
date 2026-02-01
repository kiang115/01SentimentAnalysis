"""
按不同学习率执行指定的 BERT 训练 notebook，将训练过程、测试结果及学习率输出到 test.txt。

使用方式:
  cd shop
  python run_lr_sweep.py                              # 使用默认 notebook 和学习率
  python run_lr_sweep.py --notebooks ALL_Funning.ipynb   # 仅运行指定 notebook
  python run_lr_sweep.py --notebooks ALL_Funning.ipynb LORA_Funning.ipynb
  python run_lr_sweep.py --lr 2e-5 5e-5 1e-4         # 指定学习率列表
"""

import argparse
import os
import traceback

import nbformat
from nbconvert.preprocessors import ExecutePreprocessor


# 默认要运行的 notebook，可在脚本内修改或通过 --notebooks 覆盖
NOTEBOOKS = [
    "ALL_Funning.ipynb",
    # "LORA_Funning.ipynb",
]

# 默认学习率列表，可在脚本内修改或通过 --lr 覆盖
LEARNING_RATES = [2e-5, 3e-5, 4e-5,5e-5]

RESULT_FILE = "test.txt"
CELL_TIMEOUT = 3600  # 每个 cell 最长执行时间（秒）

# 训练 cell 和测试 cell 的索引（按当前 notebook 结构）
TRAIN_CELL_IDX = 9
TEST_CELL_IDX = 10


def get_script_dir():
    """获取脚本所在目录的绝对路径"""
    return os.path.dirname(os.path.abspath(__file__))


def get_cell_stdout(cell):
    """从 cell 的 outputs 中提取 stdout 文本"""
    texts = []
    for out in cell.outputs:
        if getattr(out, "output_type", None) == "stream" and getattr(out, "name", None) == "stdout":
            texts.append(out.text)
    return "".join(texts) if texts else ""


def extract_last_epoch_line(stdout_text):
    """从训练 cell 输出中提取最后一行 [Epoch ...] 格式"""
    last_epoch = ""
    for line in stdout_text.splitlines():
        if line.strip().startswith("[Epoch ") and "Train Loss:" in line:
            last_epoch = line.strip()
    return last_epoch if last_epoch else "(无 Epoch 输出)"


def extract_outputs(nb):
    """从执行后的 notebook 中提取训练和测试相关输出"""
    if len(nb.cells) <= max(TRAIN_CELL_IDX, TEST_CELL_IDX):
        return "(notebook 结构不匹配)", "(notebook 结构不匹配)"
    train_stdout = get_cell_stdout(nb.cells[TRAIN_CELL_IDX])
    test_stdout = get_cell_stdout(nb.cells[TEST_CELL_IDX])
    last_epoch = extract_last_epoch_line(train_stdout)
    # 测试输出：保留实验名称、测试集评估结果、训练时间、最终准确率
    return last_epoch, test_stdout


def main():
    parser = argparse.ArgumentParser(description="按不同学习率执行 BERT 训练 notebook")
    parser.add_argument(
        "--notebooks",
        nargs="+",
        default=None,
        help="要运行的 notebook 列表，如 --notebooks ALL_Funning.ipynb LORA_Funning.ipynb",
    )
    parser.add_argument(
        "--lr",
        nargs="+",
        type=float,
        default=None,
        help="学习率列表，如 --lr 2e-5 5e-5 1e-4",
    )
    args = parser.parse_args()

    notebooks = args.notebooks if args.notebooks is not None else NOTEBOOKS
    learning_rates = args.lr if args.lr is not None else LEARNING_RATES

    script_dir = get_script_dir()
    os.chdir(script_dir)

    with open(RESULT_FILE, "w", encoding="utf-8") as out_f:
        for nb_path in notebooks:
            for lr in learning_rates:
                header = f"========== {nb_path} 学习率={lr} =========="
                try:
                    print(f"正在执行: {header} ...")
                    os.environ["NOTEBOOK_LR"] = str(lr)
                    nb = nbformat.read(nb_path, as_version=4)
                    ep = ExecutePreprocessor(timeout=CELL_TIMEOUT)
                    ep.preprocess(nb, {"metadata": {"path": script_dir}})
                    last_epoch, test_output = extract_outputs(nb)

                    out_f.write(f"{header}\n\n")
                    out_f.write(f"{last_epoch}\n\n")
                    out_f.write(f"{test_output}\n\n")
                    out_f.flush()
                    print(f"  完成，结果已写入 {RESULT_FILE}")
                except Exception as e:
                    err_msg = f"错误: {e}\n\n{traceback.format_exc()}"
                    out_f.write(f"{header}\n\n{err_msg}\n\n")
                    out_f.flush()
                    print(f"  执行失败: {e}")
                finally:
                    os.environ.pop("NOTEBOOK_LR", None)

    print(f"\n全部完成，结果已保存到 {os.path.join(script_dir, RESULT_FILE)}")


if __name__ == "__main__":
    main()
