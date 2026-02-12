package org.example.sentimentanalysis.exception;

import org.example.sentimentanalysis.response.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
//统一捕获所有的可能异常
//出问题吧就返回给前端，让前端来处理
public class GlobalExceptionHandler {
// 系统异常包装为响应对象返回
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        return Response.fail(e.getMessage());
    }

// 将业务异常包装为响应对象返回
    @ExceptionHandler(CustomBusinessException.class)
    public Response handleCustomException(CustomBusinessException e) {
        return Response.fail(e.getCode(), e.getMessage());
    }
}
