package com.example.eventmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseWrapper<T> {
    private T data;
    private String message;
    private boolean success;
    private String errorCode;
    public static <T> ResponseWrapper<T> success(T data , String message) {
        return ResponseWrapper.<T>builder()
                .data(data)
                .message(message)
                .success(true)
                .build();
    }

    public static <T> ResponseWrapper<T> failure(T data ,String errorCode, String errorMessage) {
        return ResponseWrapper.<T>builder()
                .data(data)
                .message(errorMessage)
                .success(false)
                .errorCode(errorCode)
                .build();
    }

}
