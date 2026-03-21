package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum CommentStatusEnum {

    PENDING(0, "待推理"),
    INFERRING(1, "推理中"),
    INFERRED(2, "已推理"),
    REVIEWING(3, "审核中"),
    CORRECTED(4, "已修正"),
    REJECTED(5, "已拒绝");

    private final int code;
    private final String message;

    CommentStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessageByCode(int code) {
        for (CommentStatusEnum status : CommentStatusEnum.values()) {
            if (status.getCode() == code) {
                return status.getMessage();
            }
        }
        return "未知状态";
    }

    public static CommentStatusEnum getByCode(int code) {
        for (CommentStatusEnum status : CommentStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
