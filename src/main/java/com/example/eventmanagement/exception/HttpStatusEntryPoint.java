package com.example.eventmanagement.exception;

import com.example.eventmanagement.dto.request.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import java.io.IOException;


public final class HttpStatusEntryPoint implements AuthenticationEntryPoint {

    private final HttpStatus httpStatus;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpStatusEntryPoint(HttpStatus httpStatus) {
        Assert.notNull(httpStatus, "httpStatus cannot be null");
        this.httpStatus = httpStatus;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(this.httpStatus.value());
        response.setContentType("application/json");

        // Try to get custom exception from request attribute
        Exception customEx = (Exception) request.getAttribute("auth_exception");

        String message = "Unauthorized access";

        if (customEx != null && customEx.getMessage() != null && !customEx.getMessage().isBlank()) {
            message = customEx.getMessage();
        } else if (authException.getMessage() != null && !authException.getMessage().isBlank()) {
            message = authException.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }


}
