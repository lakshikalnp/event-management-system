package com.example.eventmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestDto {

    private UUID eventId;
    private UUID userId;
    private String status;

}
