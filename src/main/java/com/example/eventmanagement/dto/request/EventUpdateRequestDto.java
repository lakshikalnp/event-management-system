package com.example.eventmanagement.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.ZonedDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventUpdateRequestDto {

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    @Future(message = "Start time must be in the future")
    private ZonedDateTime startTime;

    @Future(message = "End time must be in the future")
    private ZonedDateTime endTime;

    private String location;

    private String status;

}
