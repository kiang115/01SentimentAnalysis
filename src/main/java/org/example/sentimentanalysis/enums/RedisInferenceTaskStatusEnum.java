package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum RedisInferenceTaskStatusEnum {
    PENDING(0, "处理中"),
    COMPLETED(1, "正常完成"),
    ERRORTASK(2, "异常，请打印异常信息"),
    UNKNOWN(-1, "未知状态");
    private final Integer code;

    // 获取状态名称
    /**
     * 状态名称
     */
    private final String name;


    // 构造方法
    RedisInferenceTaskStatusEnum(Integer code, String name) {
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
        for (RedisInferenceTaskStatusEnum status : RedisInferenceTaskStatusEnum.values()) {
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

    public static RedisInferenceTaskStatusEnum getByCode(int code) {
        for (RedisInferenceTaskStatusEnum status : RedisInferenceTaskStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
