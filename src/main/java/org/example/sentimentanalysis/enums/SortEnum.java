package org.example.sentimentanalysis.enums;

import lombok.Getter;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum SortEnum {
    // 定义枚举常量：(Key, Description, Code)
    // 注意：这里保留了你要求的 "lastest" 拼写
    NEWEST_SORT("newest", "最新", 0),
    LATEST_SORT("lastest", "最晚", 1);

    // Getter 方法
    private final String key;         // 对应 newest, lastest
    private final String description; // 对应 最新, 最晚
    private final Integer code;       // 对应 0, 1

    // 构造函数
    SortEnum(String key, String description, Integer code) {
        this.key = key;
        this.description = description;
        this.code = code;
    }

    /**
     * 1. 检测排序参数是否存在
     * 比如输入 "newest" 返回 true
     *
     * @param key 传入的字符串参数
     * @return boolean
     */
    public static boolean containsKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        for (SortEnum e : SortEnum.values()) {
            // 使用 equals 比较自定义的 key 字段
            if (e.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 2. 根据参数(key)获得描述
     * 比如输入 "lastest" 返回 "最晚"
     *
     * @param key 传入的字符串参数
     * @return String 描述
     */
    public static String getDescByKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        for (SortEnum e : SortEnum.values()) {
            if (e.getKey().equals(key)) {
                return e.getDescription();
            }
        }
        return null; // 或者返回 "" 或 "未知"
    }

    /**
     * 3. 根据 code 获得枚举对象
     * 比如输入 0 返回 SortEnum.NEWEST_SORT 对象
     *
     * @param code 传入的数字
     * @return SortEnum 对象
     */
    public static SortEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SortEnum e : SortEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
