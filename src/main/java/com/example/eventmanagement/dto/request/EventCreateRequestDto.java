package com.example.eventmanagement.dto.request;
import com.example.eventmanagement.enumeration.Visibility;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventCreateRequestDto {

    @NotBlank(message = "required")
    @Size(max = 100, message = "must not exceed 100 characters")
    private String title;

    @NotBlank(message = "is required")
    private String description;

    @NotNull(message = "is required")
    private UUID userId;

    @NotNull(message = "is required")
    @Future(message = "must be in the future")
    private ZonedDateTime startTime;

    @NotNull(message = "is required")
    @Future(message = "must be in the future")
    private ZonedDateTime endTime;

    @NotBlank(message = "is required")
    private String location;

    private Visibility visibility;
}
