package org.example.sentimentanalysis.response;

import lombok.Getter;

@Getter
public class Response<T> {
// 这里是一些构造出消息体的方法，可以用自定义的code和message构造，也可以用ResponseCode构造，还可以只用message构造
    private T data;
    private String message;
    private Integer code;

    private Response(Integer code, String message, T data) {
        this.data = data;
        this.message = message;
        this.code = code;
    }

    private Response(T data, ResponseCode responseCode) {
        this.data = data;
        this.message = responseCode.getMessage();
        this.code = responseCode.getCode();
    }

    private Response(Integer code) {
        this.code = code;
    }

    private Response(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> Response<T> success() {
        return new Response<>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> Response<T> success(String message) {
        return new Response<>(ResponseCode.SUCCESS.getCode(), message);
    }

    public static <T> Response<T> data(T data) {
        return new Response<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Response<T> fail() {
        return new Response<>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(ResponseCode.ERROR.getCode(), message);
    }

    public static <T> Response<T> fail(ResponseCode responseCode) {
        return new Response<>(responseCode.getCode(), responseCode.getMessage());
    }

    public static <T> Response<T> fail(Integer code, String message) {
        return new Response<>(code, message);
    }

}
