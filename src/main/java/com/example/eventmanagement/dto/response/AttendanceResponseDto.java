package com.example.eventmanagement.dto.response;


import com.example.eventmanagement.entity.Attendance;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponseDto {
    private UUID userId;
    private UUID eventId;
    private String status;
    private String respondedAt;

    public static AttendanceResponseDto attendanceToDto (Attendance attendance) {
        return AttendanceResponseDto.builder()
                .status(attendance.getStatus().name())
                .userId(attendance.getUser().getId())
                .eventId(attendance.getEvent().getId())
                .respondedAt(attendance.getRespondedAt().format(AppConstants.DATE_TIME_FORMATTER))
                .build();
    }

}
