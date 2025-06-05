package com.example.eventmanagement.exception;

import com.example.eventmanagement.dto.request.ErrorResponse;
import com.example.eventmanagement.dto.response.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.eventmanagement.util.AppConstants.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        log.error(EXCEPTION_OCCURRED, ex);
        ResponseWrapper<Map<String, Object>> responseWrapper = ResponseWrapper.failure(response,VALIDATION_FAILURE_CODE,null);
        return new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseWrapper<String>> handleAccessDenied(AccessDeniedException ex) {
        log.error(EXCEPTION_OCCURRED, ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.failure(null,ACCESS_DENIED_ERROR_CODE,"You don't have permission to access this resource"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<String>> handleNotFound(ResourceNotFoundException ex) {
        log.error(EXCEPTION_OCCURRED, ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.failure(null,RESOURCE_NOT_FOUND_ERROR_CODE, ex.getMessage()));
    }

     @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseWrapper<String>> handleBadCredentials(BadCredentialsException ex) {
        log.error(EXCEPTION_OCCURRED, ex);

         return ResponseEntity
                 .status(HttpStatus.FORBIDDEN)
                 .body(ResponseWrapper.failure(null,UN_AUTHORIZED_ERROR_CODE, "Invalid credentials: " + ex.getMessage()));

    }


    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ResponseWrapper<String>> handleUnAuthorized(UnAuthorizedException ex) {
        log.error(EXCEPTION_OCCURRED, ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.failure(null, UN_AUTHORIZED_ERROR_CODE, "Un authorized: " + ex.getMessage()));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<String>> handleOtherExceptions(Exception ex) {
        log.error(EXCEPTION_OCCURRED, ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.failure(null, INTERNAL_ERROR_CODE, "Internal Server Error. Please try again later."));

    }
}
