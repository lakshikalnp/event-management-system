package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.AttendanceRequestDto;
import com.example.eventmanagement.dto.response.AttendanceResponseDto;
import com.example.eventmanagement.dto.response.ResponseWrapper;
import com.example.eventmanagement.service.AttendanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendances")
@Slf4j
@AllArgsConstructor
public class AttendanceController {

    private AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<String>> recordAttendance(@RequestBody AttendanceRequestDto request) {
        log.info("AttendanceController.recordAttendance req->{}", request.toString());
        AttendanceResponseDto attendanceResponseDto = attendanceService.saveAttendance(request);
        String responseMessage = "Attendance successfully added with userId: " +
                attendanceResponseDto.getUserId() + " eventId: " +
                attendanceResponseDto.getEventId();

        log.info("AttendanceController.recordAttendance success");

        ResponseWrapper<String> response = new ResponseWrapper<>(
                null,
                responseMessage,
                true
        );

        return ResponseEntity.ok(response);
    }

}
