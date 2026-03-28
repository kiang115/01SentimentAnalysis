package org.example.sentimentanalysis.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数据源枚举类
 * 包含corrected(人工修正数据集)、upload(手动上传书籍)、original(原始数据集)
 */
@Getter
public enum TrainDataSourceEnum {
    // 枚举值定义：英文标识 + 中文描述
    CORRECTED("corrected", "人工修正数据集"),
    UPLOAD("upload", "手动上传数据集"),
    ORIGINAL("original", "原始数据集");

    // 枚举属性
    private final String code;    // 英文标识
    private final String desc;    // 中文描述

    // 构造方法
    TrainDataSourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // ========== 核心功能：根据英文标识查找对应的枚举/中文描述 ==========

    /**
     * 根据英文标识获取对应的枚举对象
     * @param code 英文标识（如"corrected"）
     * @return 对应的枚举对象，不存在则返回null
     */
    public static TrainDataSourceEnum getByCode(String code) {
        // 判空，避免空指针
        if (Objects.isNull(code)) {
            return null;
        }
        // 遍历所有枚举值匹配code
        return Arrays.stream(values())
                .filter(enumObj -> enumObj.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据英文标识获取对应的中文描述
     * @param code 英文标识（如"corrected"）
     * @return 中文描述，不存在则返回空字符串
     */
    public static String getDescByCode(String code) {
        TrainDataSourceEnum enumObj = getByCode(code);
        return enumObj != null ? enumObj.getDesc() : "";
    }

    // ========== 核心功能：判断英文标识是否存在 ==========

    /**
     * 判断传入的英文标识是否存在于枚举中
     * @param code 英文标识（如"corrected"）
     * @return 存在返回true，否则返回false
     */
    public static boolean isCodeExist(String code) {
        return getByCode(code) != null;
    }
}