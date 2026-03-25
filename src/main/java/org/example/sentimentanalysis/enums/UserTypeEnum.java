package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {
    CONSUMER("consumer", "顾客"),
    MERCHANT("merchant", "商家"),
    ADMIN("admin", "管理员");

    private final String code;
    private final String desc;

    UserTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isCodeExist(String code) {
        for (UserTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

