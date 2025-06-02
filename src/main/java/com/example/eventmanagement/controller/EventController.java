package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventFilterRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventResponseDto;
import com.example.eventmanagement.dto.response.EventDetailsWithAttendeeCountResponseDto;
import com.example.eventmanagement.dto.response.ResponseWrapper;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<EventResponseDto>>> getEventsWithFiltering(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        EventFilterRequestDto filter = new EventFilterRequestDto(date, visibility, location);
        List<EventResponseDto> result = eventService.getEventsWithFiltering(filter);

        ResponseWrapper<List<EventResponseDto>> response = new ResponseWrapper<>(
                result,
                "Successfully fetched",
                true
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<String>> createEvent(@RequestBody @Valid EventCreateRequestDto requestDto) {
        log.info("EventController.createEvent req->{}", requestDto.toString());
        EventResponseDto responseDto = eventService.saveEvent(requestDto);
        log.info("EventController.createEvent res->{}", responseDto.toString());
        ResponseWrapper<String> response = new ResponseWrapper<>(
                null,
                "Event successfully added with id: "+responseDto.getId(),
                true
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<ResponseWrapper<String>> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateRequestDto requestDto) {
        log.info("EventController.updateEvent req->{}", requestDto.toString());
        eventService.updateEvent(eventId, requestDto);
        log.info("EventController.updateEvent updated successfully eventId: {}",eventId);
        ResponseWrapper<String> response = new ResponseWrapper<>(
                null,
                "Event successfully updated with id: "+eventId,
                true
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<ResponseWrapper<String>> deleteEvent(
            @PathVariable UUID eventId) {
        log.info("EventController.deleteEvent eventId ->{}", eventId);
        eventService.updateEventStatus(eventId, EventStatus.DELETED.name());
        log.info("EventController.deleteEvent eventId ->{} success", eventId);
        ResponseWrapper<String> response = new ResponseWrapper<>(
                null,
                "Event successfully deleted with id: "+eventId,
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventResponseDto>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("EventController.getUpcomingEvents page ->{}, size ->{}", page, size);
        return ResponseEntity.ok(eventService.listUpcomingEvents(page, size));
    }

    @GetMapping("/{eventId}/status")
    public ResponseEntity<ResponseWrapper<String>> getStatus(@PathVariable UUID eventId) {
        log.info("EventController.getStatus eventId ->{}", eventId);
        String status = eventService.statusCheckOfAnEvent(eventId);
        log.info("EventController.getStatus eventId ->{}, status ->{}", eventId, status);
        ResponseWrapper<String> response = new ResponseWrapper<>(
                status,
                "Successfully fetched",
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<ResponseWrapper<List<EventResponseDto>>> getAllUserEvents(@PathVariable UUID userId) {
        log.info("EventController.getAllUserEvents userId ->{}", userId);
        List<EventResponseDto> events = eventService.getAllEventsForUser(userId);
        log.info("EventController.getAllUserEvents events.size ->{}", events.size());
        ResponseWrapper<List<EventResponseDto>> response = new ResponseWrapper<>(
                events,
                "Successfully fetched",
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/details")
    public ResponseEntity<ResponseWrapper<EventDetailsWithAttendeeCountResponseDto>> getEventWithAttendees(@PathVariable UUID eventId) {
        log.info("EventController.getEventWithAttendees eventId ->{}", eventId);
        EventDetailsWithAttendeeCountResponseDto eventWithAttendeeCount = eventService.getEventWithAttendeeCount(eventId);
        ResponseWrapper<EventDetailsWithAttendeeCountResponseDto> response = new ResponseWrapper<>(
                eventWithAttendeeCount,
                "Successfully fetched",
                true
        );

        return ResponseEntity.ok(response);
    }

}
