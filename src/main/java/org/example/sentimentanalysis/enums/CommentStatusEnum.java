package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum CommentStatusEnum {

    // 定义枚举常量
    PENDING(0, "待推理(pending)"),
    INFERRING(1, "推理中(inferring)"),
    INFERRED(2, "已推理(inferred)"),
    REVIEWING(3, "审核中(reviewing)"),
    CORRECTED(4, "已修正(corrected)"),
    REJECTED(5, "已拒绝(rejected)");

    // Getter 方法
    // 成员变量
    private final int code;
    private final String message;

    // 构造方法
    CommentStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据 code 获取对应的 message
     *
     * @param code 状态码
     * @return 对应的描述信息，如果未找到则返回 null 或 "未知状态"
     */
    public static String getMessageByCode(int code) {
        for (CommentStatusEnum status : CommentStatusEnum.values()) {
            if (status.getCode() == code) {
                return status.getMessage();
            }
        }
        return "未知状态"; // 或者返回 null，视业务需求而定
    }

    /**
     * 根据 code 获取对应的枚举对象（用于业务逻辑判断）
     *
     * @param code 状态码
     * @return 对应的枚举对象
     */
    public static CommentStatusEnum getByCode(int code) {
        for (CommentStatusEnum status : CommentStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
