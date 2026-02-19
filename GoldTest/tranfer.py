import pandas as pd


def convert_csv_format(input_file, output_file):
    """
    将包含label和review列的CSV转换为包含data和label列的CSV

    参数:
        input_file: 输入CSV文件路径
        output_file: 输出CSV文件路径
    """
    # 读取原始CSV文件
    df = pd.read_csv(input_file)

    # 检查必要的列是否存在
    if 'label' not in df.columns or 'review' not in df.columns:
        raise ValueError("CSV文件必须包含'label'和'review'列")

    # 创建新的DataFrame，重命名列
    new_df = pd.DataFrame({
        'data': df['review'],
        'label': df['label']
    })

    # 保存为新的CSV文件（使用默认逗号分隔符）
    new_df.to_csv(output_file, index=False, encoding='utf-8')

    print(f"转换完成！结果已保存到: {output_file}")
    print(f"新文件包含 {len(new_df)} 条记录")
    print(f"列名: {', '.join(new_df.columns.tolist())}")


# 使用示例
convert_csv_format('waimai.csv', 'waimai.csv')
