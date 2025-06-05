package com.example.eventmanagement.util;

import com.example.eventmanagement.dto.response.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionUtil {

    private ExceptionUtil () {}
    public static <T> ResponseEntity<ResponseWrapper<T>> buildError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ResponseWrapper.failure(null, code, message));
    }

}
