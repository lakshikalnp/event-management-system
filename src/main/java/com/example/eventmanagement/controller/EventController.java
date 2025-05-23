package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventFilterRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventCreateResponseDto;
import com.example.eventmanagement.dto.response.EventDetailsWithAttendeeCountResponseDto;
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
    public ResponseEntity<List<EventCreateResponseDto>> getEventsWithFiltering(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        EventFilterRequestDto filter = new EventFilterRequestDto(date, visibility, location);
        List<EventCreateResponseDto> result = eventService.getEventsWithFiltering(filter);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<String> createEvent(@RequestBody @Valid EventCreateRequestDto requestDto) {
        log.info("EventController.createEvent req->{}", requestDto.toString());
        EventCreateResponseDto responseDto = eventService.saveEvent(requestDto);
        log.info("EventController.createEvent res->{}", responseDto.toString());
        return ResponseEntity.ok("Event successfully added with id: "+responseDto.getId());
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<String> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateRequestDto requestDto) {
        log.info("EventController.updateEvent req->{}", requestDto.toString());
        EventCreateResponseDto updated = eventService.updateEvent(eventId, requestDto);
        log.info("EventController.updateEvent updated successfully eventId: {}",eventId);
        return ResponseEntity.ok("Event successfully updated with id: "+eventId);
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<String> deleteEvent(
            @PathVariable UUID eventId) {
        log.info("EventController.deleteEvent eventId ->{}", eventId);
        eventService.updateEventStatus(eventId, EventStatus.DELETED.name());
        log.info("EventController.deleteEvent eventId ->{} success", eventId);
        return ResponseEntity.ok("Event successfully deleted with id: "+eventId);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventCreateResponseDto>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("EventController.getUpcomingEvents page ->{}, size ->{}", page, size);
        return ResponseEntity.ok(eventService.listUpcomingEvents(page, size));
    }

    @GetMapping("/{eventId}/status")
    public ResponseEntity<String> getStatus(@PathVariable UUID eventId) {
        log.info("EventController.getStatus eventId ->{}", eventId);
        String status = eventService.statusCheckOfAnEvent(eventId);
        log.info("EventController.getStatus eventId ->{}, status ->{}", eventId, status);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<EventCreateResponseDto>> getAllUserEvents(@PathVariable UUID userId) {
        log.info("EventController.getAllUserEvents userId ->{}", userId);
        List<EventCreateResponseDto> events = eventService.getAllEventsForUser(userId);
        log.info("EventController.getAllUserEvents events.size ->{}", events.size());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/details")
    public ResponseEntity<EventDetailsWithAttendeeCountResponseDto> getEventWithAttendees(@PathVariable UUID eventId) {
        log.info("EventController.getEventWithAttendees eventId ->{}", eventId);
        return ResponseEntity.ok(eventService.getEventWithAttendeeCount(eventId));
    }

}
