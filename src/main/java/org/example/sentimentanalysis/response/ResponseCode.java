package org.example.sentimentanalysis.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * 这里只是存储所有状态码和信息的地方，不是要返回的对象
 **/
@AllArgsConstructor
@Getter
public enum ResponseCode {
    //在这里自定义所有输出的状态码和信息
    SUCCESS("操作成功", 200),
    ERROR("操作失败", 500),
    NOT_FOUND("没有找到此数据",210 );

    private final String message;
    private final Integer code;
}
