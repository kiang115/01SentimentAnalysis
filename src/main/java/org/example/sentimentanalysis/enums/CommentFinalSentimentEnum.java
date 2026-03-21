package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum CommentFinalSentimentEnum {
    NEGATIVE(0, "\u5DEE\u8BC4"),
    POSITIVE(1, "\u597D\u8BC4");

    private final int code;
    private final String name;

    CommentFinalSentimentEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CommentFinalSentimentEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentFinalSentimentEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        CommentFinalSentimentEnum item = getByCode(code);
        return item == null ? null : item.getName();
    }
}
