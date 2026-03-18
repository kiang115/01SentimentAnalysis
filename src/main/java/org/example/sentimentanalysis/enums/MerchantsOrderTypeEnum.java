package org.example.sentimentanalysis.enums;

/**
 * 商家列表排序方式
 */
public enum MerchantsOrderTypeEnum {
    RATING("rating"),
    POSITIVE_RATE("positive"),
    COMMENT_COUNT("commentNum"),
    DEFAULT("default");

    private final String code;

    MerchantsOrderTypeEnum(String code) {
        this.code = code;
    }

    public static MerchantsOrderTypeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return RATING;
        }
        for (MerchantsOrderTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return RATING;
    }
}

