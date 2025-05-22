package com.example.eventmanagement.dto.request;
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

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private ZonedDateTime endTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "PUBLIC|PRIVATE", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility;
}
