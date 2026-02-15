package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum InferenceTaskStatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 成功
     */
    SUCCESS(2, "已完成"),

    /**
     * 失败
     */
    FAILED(3, "失败"),

    UNKNOWN(-1, "未知状态");
    // 获取状态编码
    /**
     * 状态编码
     */
    private final Integer code;

    // 获取状态名称
    /**
     * 状态名称
     */
    private final String name;


    // 构造方法
    InferenceTaskStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据状态编码获取对应的状态名称（核心转换方法）
     *
     * @param statusCode 状态编码（0/1/2/3）
     * @return 状态名称（如：待处理/处理中）
     */
    public static String getStatusNameByCode(Integer statusCode) {
        // 遍历枚举值，匹配编码
        for (InferenceTaskStatusEnum status : InferenceTaskStatusEnum.values()) {
            if (status.getCode().equals(statusCode)) {
                return status.getName();
            }
        }
        // 未匹配到返回未知状态
        return UNKNOWN.getName();
    }

    /**
     * 判断状态编码是否存在
     *
     * @param code 状态编码
     * @return 是否存在
     */
    public static boolean existsByCode(Integer code) {
        if (code == null) {
            return false;
        }
        return getByCode(code) != null;
    }

    public static InferenceTaskStatusEnum getByCode(int code) {
        for (InferenceTaskStatusEnum status : InferenceTaskStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
