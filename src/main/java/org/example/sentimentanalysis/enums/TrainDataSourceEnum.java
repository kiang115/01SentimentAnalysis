package org.example.sentimentanalysis.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum TrainDataSourceEnum {
    CORRECTED("corrected"),
    UPLOAD("upload"),
    ORIGINAL("original");

    private final String code;

    TrainDataSourceEnum(String code) {
        this.code = code;
    }

    public static List<String> allCodes() {
        return Arrays.stream(values())
                .map(TrainDataSourceEnum::getCode)
                .collect(Collectors.toList());
    }
}
