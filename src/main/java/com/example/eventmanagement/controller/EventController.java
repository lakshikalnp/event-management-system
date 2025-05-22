package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventCreateResponseDto;
import com.example.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {


    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventCreateResponseDto> createEvent(@RequestBody @Valid EventCreateRequestDto requestDto) {
        log.info("EventController.createEvent req->{}", requestDto.toString());
        EventCreateResponseDto responseDto = eventService.saveEvent(requestDto);
        log.info("EventController.createEvent res->{}", responseDto.toString());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<EventCreateResponseDto> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateRequestDto requestDto) {

        EventCreateResponseDto updated = eventService.updateEvent(eventId, requestDto);
        return ResponseEntity.ok(updated);
    }

}
