package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.AttendanceRequestDto;
import com.example.eventmanagement.dto.response.AttendanceResponseDto;
import com.example.eventmanagement.entity.Attendance;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Status;
import com.example.eventmanagement.exception.ResourceNotFoundException;
import com.example.eventmanagement.repository.AttendanceRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;


    public AttendanceResponseDto saveAttendance(AttendanceRequestDto attendanceRequestDto) {
        log.info("saveAttendance called");
        //get event and use details
        Event event = eventRepository.findByIdAndStatus(attendanceRequestDto.getEventId(), EventStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + attendanceRequestDto.getEventId()));
        User user = userRepository.findById(attendanceRequestDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + attendanceRequestDto.getUserId()));

        //set attendance object
        Attendance attendance = new Attendance();
        attendance.setEvent(event);
        attendance.setUser(user);
        attendance.setStatus(Status.valueOf(attendanceRequestDto.getStatus()));
        attendance.setRespondedAt(ZonedDateTime.now());
        attendance = attendanceRepository.saveAndFlush(attendance);

        log.info("Attendance added successfully");
        return AttendanceResponseDto.attendanceToDto(attendance);
    }
}
