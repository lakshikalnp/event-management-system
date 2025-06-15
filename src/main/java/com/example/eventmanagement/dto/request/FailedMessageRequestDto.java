package com.example.eventmanagement.dto.request;

import java.time.ZonedDateTime;
import java.util.UUID;

public record FailedMessageRequestDto(UUID id, String title, String errorDescription, Integer retryAttempts, String payLoad, ZonedDateTime updatedAt, ZonedDateTime createdAt, boolean sendToDlq, Integer maxRetryAttempts){}