package com.example.eventmanagement.exception;

import com.example.eventmanagement.dto.response.ResponseWrapper;
import com.example.eventmanagement.util.ExceptionUtil;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.example.eventmanagement.util.AppConstants.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();

        log.error(EXCEPTION_OCCURRED, ex);

        return ResponseEntity
                .badRequest()
                .body(ResponseWrapper.failure(errors, VALIDATION_FAILURE_CODE, "Validation failed"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseWrapper<String>> handleAccessDenied(AccessDeniedException ex) {
        log.error(EXCEPTION_OCCURRED, ex);

        return ExceptionUtil.buildError(HttpStatus.FORBIDDEN, ACCESS_DENIED_ERROR_CODE,
                "You don't have permission to access this resource");

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<String>> handleNotFound(ResourceNotFoundException ex) {
        log.error(EXCEPTION_OCCURRED, ex);
        return ExceptionUtil.buildError(HttpStatus.NOT_FOUND,RESOURCE_NOT_FOUND_ERROR_CODE, ex.getMessage());

    }

     @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseWrapper<String>> handleBadCredentials(BadCredentialsException ex) {
        log.error(EXCEPTION_OCCURRED, ex);
         return ExceptionUtil.buildError(HttpStatus.FORBIDDEN, UN_AUTHORIZED_ERROR_CODE, "Invalid credentials: "
                 + ex.getMessage());

    }


    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ResponseWrapper<String>> handleUnAuthorized(UnAuthorizedException ex) {
        log.error(EXCEPTION_OCCURRED, ex);
        return ExceptionUtil.buildError(HttpStatus.FORBIDDEN, UN_AUTHORIZED_ERROR_CODE, "Un authorized: "
                + ex.getMessage());

    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ResponseWrapper<String>> handleUnexpectedTypeException(UnexpectedTypeException ex) {
        log.error(EXCEPTION_OCCURRED, ex);
        return ExceptionUtil.buildError(HttpStatus.BAD_REQUEST, VALIDATION_FAILURE_CODE,
                ex.getMessage());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<String>> handleOtherExceptions(Exception ex) {
        log.error(EXCEPTION_OCCURRED, ex);
        return ExceptionUtil.buildError(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_CODE,
                "Internal Server Error. Please try again later.");

    }
}
