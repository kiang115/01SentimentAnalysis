package org.example.sentimentanalysis.enums;

import lombok.Getter;

@Getter
public enum ModelSourceEnum {
    TRAIN("train", "训练模型"),
    UPLOAD("upload", "手动上传");

    private final String code;
    private final String desc;

    ModelSourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (ModelSourceEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        return code;
    }

    public static boolean isCodeExist(String code) {
        for (ModelSourceEnum e : values()) {
            if (e.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
