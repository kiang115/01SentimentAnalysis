# 数据库设计说明

以下内容依据 `springboot/src/main/resources/doc/sql.txt` 中的建表语句整理，仅列出各表最重要的字段用于数据库设计说明。表格中的约束含义如下：

- `P`：主键
- `U`：唯一约束
- `F`：业务关联字段（逻辑外键）

## 5.1 用户信息表

用户信息表主要用于存储系统登录用户的基础资料、账号状态以及角色类型，支撑系统完成身份认证、权限区分和登录行为记录。商家账号还可以通过该表与商铺信息建立关联，因此该表是整个平台用户管理与权限控制的基础。其关键字段信息如表 5-1 所示。

表 5-1 商户评论情感分析系统用户信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| user_id | bigint | N | P | 用户唯一标识 |
| user_name | varchar(50) | N | U | 登录用户名 |
| password_hash | varchar(255) | N |  | 密码哈希值 |
| user_type | enum('consumer','merchant','admin') | N |  | 用户角色类型 |
| merchant_id | bigint | Y | F | 关联商家编号，仅商家角色使用 |
| status | tinyint | N |  | 账号状态，1 为正常，0 为禁用 |
| last_login_time | datetime | Y |  | 最后登录时间 |
| create_time | datetime | Y |  | 创建时间 |

## 5.2 商家信息表

商家信息表用于存储平台中商户主体的基础资料、所属业务领域以及口碑统计结果，为商家展示、评论归档、商品挂接和排行分析提供数据支撑。该表与用户表、商品表、评论表共同构成商家业务主线。其关键字段信息如表 5-2 所示。

表 5-2 商户评论情感分析系统商家信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| merchants_id | bigint | N | P | 商家唯一标识 |
| name | varchar(100) | N |  | 商家名称 |
| domain_id | bigint | N | F | 所属领域编号 |
| domain_name | varchar(100) | N |  | 所属领域名称 |
| rating | decimal(3,2) | Y |  | 商家综合得分 |
| comment_count | bigint | Y |  | 总评论数 |
| inferred_count | bigint | Y |  | 已完成推理的评论数 |
| positive_rate | decimal(5,4) | Y |  | 好评率 |
| create_time | datetime | Y |  | 创建时间 |

## 5.3 商品信息表

商品信息表用于存储商家下具体商品或服务对象的基础信息，并记录其口碑得分、评论量和情感分析统计结果。系统可基于该表实现商品级评论分析、商品排行展示以及商家商品画像构建。其关键字段信息如表 5-3 所示。

表 5-3 商户评论情感分析系统商品信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| products_id | bigint | N | P | 商品唯一标识 |
| merchant_id | bigint | N | F | 所属商家编号 |
| name | varchar(150) | N |  | 商品名称 |
| domain_id | bigint | N | F | 所属领域编号 |
| price | decimal(10,2) | N |  | 商品价格 |
| rating | decimal(3,2) | Y |  | 商品综合得分 |
| comment_count | bigint | Y |  | 商品评论数 |
| inferred_count | bigint | Y |  | 已推理评论数 |
| positive_rate | decimal(5,4) | Y |  | 商品好评率 |

## 5.4 标签信息表

标签信息表用于统一存储系统在评论分析过程中抽取或维护的标签内容，为商品标签统计、标签画像展示以及高频评价词分析提供标准化标签基础。该表与标签统计表配合使用，形成标签维度的分析能力。其关键字段信息如表 5-4 所示。

表 5-4 商户评论情感分析系统标签信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| tag_id | bigint | N | P | 标签主键编号 |
| tag_name | varchar(64) | N |  | 标签名称 |
| create_time | datetime | Y |  | 标签创建时间 |

## 5.5 标签统计表

标签统计表用于记录某一商品在某一标签维度下的正向、负向以及总评论次数，是构建商品标签画像和统计用户关注点的重要中间结果表。系统可以基于该表快速得到商品在不同评价标签上的情感倾向分布。其关键字段信息如表 5-5 所示。

表 5-5 商户评论情感分析系统标签统计表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| id | bigint | N | P | 统计记录编号 |
| product_id | bigint | N | F | 商品编号 |
| tag_id | bigint | N | F | 标签编号 |
| positive_count | int | N |  | 正向评论计数 |
| negative_count | int | N |  | 负向评论计数 |
| total_count | int | N |  | 评论总计数 |
| update_time | datetime | Y |  | 最后更新时间 |

## 5.6 评论信息表

评论信息表用于存储用户发表的原始评论内容、评论归属对象以及情感分析处理状态，是整个情感分析业务链路的核心数据来源。系统后续的推理、纠错、重训和统计分析都围绕该表展开，因此该表承担了评论数据承载与流程流转控制的双重作用。其关键字段信息如表 5-6 所示。

表 5-6 商户评论情感分析系统评论信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| comment_id | bigint | N | P | 评论唯一标识 |
| customer_id | bigint | N | F | 评论用户编号 |
| content | text | N |  | 评论内容 |
| domain_id | bigint | N | F | 评论所属领域编号 |
| product_id | bigint | N | F | 关联商品编号 |
| merchant_id | bigint | N | F | 关联商家编号 |
| status | integer | N |  | 评论处理状态 |
| final_sentiment | integer | Y |  | 最终情感结果，1 为好评，0 为差评 |
| is_retrained | boolean | N |  | 是否已进入重训数据流程 |
| publish_time | datetime | N |  | 评论发布时间 |

## 5.7 推理记录表

推理记录表用于存储模型对评论执行情感推理后的结果明细，包括所用模型、推理概率、最终模型判断和置信度等信息。该表既可用于结果追溯，也可用于模型效果评估和后续纠错分析，是模型输出落库的关键表。其关键字段信息如表 5-7 所示。

表 5-7 商户评论情感分析系统推理记录表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| inference_id | bigint | N | P | 推理记录编号 |
| inference_task_id | bigint | N | F | 推理任务编号 |
| comment_id | bigint | N | F | 评论编号 |
| model_id | bigint | N | F | 模型编号 |
| domain_id | bigint | N | F | 所属领域编号 |
| positive_prob | decimal(5,4) | N |  | 正向概率 |
| negative_prob | decimal(5,4) | N |  | 负向概率 |
| model_sentiment | integer | N |  | 模型判断结果 |
| confidence | decimal(4,3) | N |  | 推理置信度 |
| inference_time | datetime | N |  | 推理完成时间 |

## 5.8 推理任务表

推理任务表用于记录一次批量情感推理任务的发起人、处理范围、执行耗时和处理状态，便于系统对推理过程进行任务级调度、监控和结果汇总。该表与推理记录表组合后，可以形成从任务到单条评论结果的完整追踪链路。其关键字段信息如表 5-8 所示。

表 5-8 商户评论情感分析系统推理任务表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| task_id | bigint | N | P | 推理任务编号 |
| initiator_id | bigint | N | F | 任务发起人编号 |
| inference_start_time | datetime | Y |  | 推理开始时间 |
| inference_end_time | datetime | Y |  | 推理结束时间 |
| processed_count | bigint | Y |  | 处理评论总数 |
| avg_process_speed | decimal(10,2) | Y |  | 平均处理速度 |
| used_model_ids | json | Y |  | 使用模型编号列表 |
| domain_ids | json | Y |  | 涉及领域编号列表 |
| status | integer | Y |  | 任务状态 |
| status_msg | text | Y |  | 状态说明信息 |

## 5.9 模型信息表

模型信息表用于管理系统中的情感分析模型版本、来源、推理表现和生命周期状态，为模型选择、模型对比以及训练结果沉淀提供统一的数据管理入口。该表也是训练任务与推理任务之间的重要连接点。其关键字段信息如表 5-9 所示。

表 5-9 商户评论情感分析系统模型信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| model_id | bigint | N | P | 模型编号 |
| domain_id | bigint | N | F | 适用领域编号 |
| model_version | varchar(20) | N |  | 模型版本号 |
| source | enum('train','upload') | N |  | 模型来源 |
| inferred_num | bigint | N |  | 总推理次数 |
| corrected_num | bigint | N |  | 被修正次数 |
| deleted | integer | N |  | 删除标记，1 为已删除 |
| base_model_id | bigint | Y | F | 基础模型编号 |
| accuracy | decimal(5,2) | Y |  | 模型最终准确率 |
| created_at | datetime | N |  | 创建时间 |

## 5.10 领域信息表

领域信息表用于维护系统支持的业务领域信息，例如外卖、电商、酒店等，为商家、商品、评论、模型和训练任务提供统一的业务分类依据。通过该表，系统能够实现多领域情感分析能力的隔离与扩展。其关键字段信息如表 5-10 所示。

表 5-10 商户评论情感分析系统领域信息表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| domain_id | bigint | N | P | 领域编号 |
| domain_name | varchar(100) | N |  | 领域名称 |
| domain_url | varchar(100) | N |  | 领域模型路径标识 |
| domain_description | text | Y |  | 领域描述 |
| domain_image_url | varchar(200) | Y |  | 领域图片地址 |
| created_at | datetime | Y |  | 创建时间 |

## 5.11 训练任务表

训练任务表用于记录一次模型训练过程的业务来源、训练参数、执行状态以及评估指标，是模型持续优化和训练过程追溯的核心管理表。系统可以通过该表统一管理训练任务发起、执行监控、训练结果落库及指标展示。其关键字段信息如表 5-11 所示。

表 5-11 商户评论情感分析系统训练任务表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| id | bigint | N | P | 训练任务编号 |
| creator_id | bigint | N | F | 任务发起人编号 |
| domain_id | bigint | N | F | 训练所属领域编号 |
| status | integer | N |  | 训练状态 |
| start_time | datetime | Y |  | 训练开始时间 |
| end_time | datetime | Y |  | 训练结束时间 |
| total_data | bigint | Y |  | 训练样本总数 |
| if_over_train | integer | Y |  | 是否重新训练 |
| model_id | bigint | Y | F | 训练产出的模型编号 |
| model_version | varchar(20) | Y |  | 训练产出模型版本号 |
| accuracy | decimal(5,4) | Y |  | 总体准确率 |
| f1_score | decimal(5,4) | Y |  | F1 分数 |

## 5.12 训练参数配置表

训练参数配置表用于存储不同领域训练任务可复用的参数模板，包括 LoRA 参数、批次大小、学习率和数据集划分比例等内容。该表能够降低重复录入成本，并保证同一领域训练参数配置的一致性。其关键字段信息如表 5-12 所示。

表 5-12 商户评论情感分析系统训练参数配置表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| para_id | bigint | N | P | 参数配置编号 |
| domain_id | bigint | N | F | 适用领域编号 |
| lora_r | int | N |  | LoRA 的秩参数 |
| lora_alpha | int | N |  | LoRA 缩放参数 |
| epochs | int | N |  | 训练轮数 |
| batch_size | int | N |  | 训练批次大小 |
| learning_rate | decimal(10,8) | N |  | 学习率 |
| random_seed | int | N |  | 随机种子 |
| train_split_ratio | decimal(3,2) | N |  | 训练集划分比例 |
| create_time | datetime | Y |  | 创建时间 |

## 5.13 训练数据表

训练数据表用于存储模型训练所需的样本文本、标签和来源信息，是模型训练与重训练的数据基础。系统可通过该表区分人工修正数据、手动上传数据和原始数据集数据，从而为训练数据治理和样本质量分析提供支持。其关键字段信息如表 5-13 所示。

表 5-13 商户评论情感分析系统训练数据表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| id | bigint | N | P | 训练数据编号 |
| domain_id | bigint | N | F | 所属领域编号 |
| content | text | N |  | 训练文本内容 |
| label | int | N |  | 情感标签，0 为差评，1 为好评 |
| source | enum('corrected','upload','original') | N |  | 训练数据来源 |
| train_count | int unsigned | N |  | 被用于训练的次数 |
| created_at | datetime | N |  | 数据创建时间 |

## 5.14 口碑历史记录表

口碑历史记录表用于周期性存储商家或商品在某一时点上的口碑统计快照，包括评论量、已推理数量、综合得分、好评率和排名等指标。通过该表，系统可以实现口碑变化趋势分析、历史对比和可视化展示。其关键字段信息如表 5-14 所示。

表 5-14 商户评论情感分析系统口碑历史记录表

| 字段名称 | 字段类型 | 是否为空 | 约束 | 字段备注 |
| --- | --- | --- | --- | --- |
| id | bigint | N | P | 历史记录编号 |
| target_id | bigint | N | F | 商家或商品编号 |
| type | tinyint | N |  | 统计对象类型，0 为商家，1 为商品 |
| inferred_count | bigint | Y |  | 当前已推理评论数 |
| comment_count | bigint | Y |  | 当前总评论数 |
| rating | decimal(3,2) | Y |  | 当前综合得分 |
| positive_rate | decimal(5,4) | Y |  | 当前好评率 |
| ranking | bigint | N |  | 当前领域排名 |
| create_time | datetime | Y |  | 快照创建时间 |
