package com.maveric.hr360.Exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class CustomException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String message;

    public CustomException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;


    }
}
