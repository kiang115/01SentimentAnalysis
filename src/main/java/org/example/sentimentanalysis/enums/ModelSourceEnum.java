package org.example.sentimentanalysis.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum ModelSourceEnum {
    TRAIN("train", "训练得到"),
    UPLOAD("upload", "手动上传");

    private final String code;
    private final String desc;

    // 构造方法
    ModelSourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
