package org.example.sentimentanalysis.exception;

import lombok.Data;
import org.example.sentimentanalysis.response.ResponseCode;

@Data
public class CustomBusinessException extends RuntimeException {
    private Integer code;
    private String message;


    public CustomBusinessException(Integer code, String message) {
//        super(message);
        this.code = code;
        this.message = message;
    }
    public CustomBusinessException(String message) {
        this.code = ResponseCode.ERROR.getCode();
        this.message = message;
    }

    public CustomBusinessException(ResponseCode responseCode) {
//        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }
}
