package org.example.sentimentanalysis.exception;

import lombok.Data;
import org.example.sentimentanalysis.response.ResponseCode;

@Data
public class CustomExceptionHandler extends RuntimeException {
    private Integer code;
    private String message;


    public CustomExceptionHandler(Integer code, String message) {
//        super(message);
        this.code = code;
        this.message = message;
    }
    public CustomExceptionHandler(String message) {
        this.code = ResponseCode.ERROR.getCode();
        this.message = message;
    }

    public CustomExceptionHandler(ResponseCode responseCode) {
//        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }
}
