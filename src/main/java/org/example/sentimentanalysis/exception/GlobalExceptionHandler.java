package org.example.sentimentanalysis.exception;

import org.example.sentimentanalysis.response.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
//统一捕获所有的可能异常
//出问题吧就返回给前端，让前端来处理
public class GlobalExceptionHandler {
    // 系统异常包装为响应对象返回
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        e.printStackTrace();
        return Response.fail(e.getMessage());
    }

    // 将业务异常包装为响应对象返回
    @ExceptionHandler(CustomBusinessException.class)
    public Response handleCustomException(CustomBusinessException e) {
        e.printStackTrace();
        return Response.fail(e.getCode(), e.getMessage());
    }
//   导入文件处理异常
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        if (e.getMessage().contains("Broken pipe") || e.getMessage().contains("中止")) {
            // 客户端取消了下载，直接忽略，不写回任何数据
            System.out.println("客户端主动取消了下载连接");
        } else {
            System.out.println("IO异常");
        }
    }
//    #todo  对satoken的异常进行手动捕获处理
}
