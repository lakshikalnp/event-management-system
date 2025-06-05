package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventFilterRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventResponseDto;
import com.example.eventmanagement.dto.response.EventDetailsWithAttendeeCountResponseDto;
import com.example.eventmanagement.dto.response.ResponseWrapper;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.eventmanagement.util.AppConstants.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<EventResponseDto>>> getEventsWithFiltering(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("EventController.getEventsWithFiltering req-> location {}, visibility {}, date {}", location , null!=visibility?visibility.toString():null, date);
        EventFilterRequestDto filter = new EventFilterRequestDto(date, null!=visibility?visibility.toString():null, location);
        List<EventResponseDto> result = eventService.getEventsWithFiltering(filter);

        ResponseWrapper<List<EventResponseDto>> response = ResponseWrapper.success(result,  SUCCESSFULLY_FETCHED);
        log.info("EventController.getEventsWithFiltering res-> success");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<String>> createEvent(@RequestBody @Valid EventCreateRequestDto requestDto) {
        log.info("EventController.createEvent req-> title {}, description {}", requestDto.getTitle(), requestDto.getDescription());
        EventResponseDto responseDto = eventService.saveEvent(requestDto);
        log.info("EventController.createEvent res->id {}", responseDto.getId());
        ResponseWrapper<String> response = ResponseWrapper.success(null,  SUCCESSFULLY_CREATED_AN_EVENT+responseDto.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<ResponseWrapper<String>> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateRequestDto requestDto) {
        log.info("EventController.updateEvent req ->{}", requestDto.toString());
        eventService.updateEvent(eventId, requestDto);
        log.info("EventController.updateEvent updated successfully eventId: {}",eventId);
        ResponseWrapper<String> response = ResponseWrapper.success(null,  SUCCESSFULLY_UPDATED_AN_EVENT+eventId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<ResponseWrapper<String>> deleteEvent(
            @PathVariable UUID eventId) {
        log.info("EventController.deleteEvent eventId ->{}", eventId);
        eventService.updateEventStatus(eventId, EventStatus.DELETED.name());
        log.info("EventController.deleteEvent eventId ->{} success", eventId);
        ResponseWrapper<String> response = ResponseWrapper.success(null, SUCCESSFULLY_DELETED_AN_EVENT+eventId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ResponseWrapper<Page<EventResponseDto>>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("EventController.getUpcomingEvents page ->{}, size ->{}", page, size);
        Page<EventResponseDto> eventResponseDtos = eventService.listUpcomingEvents(page, size);
        log.info("EventController.getUpcomingEvents tot elements ->{}", eventResponseDtos.getTotalElements());
        return ResponseEntity.ok(ResponseWrapper.success(eventResponseDtos, SUCCESSFULLY_FETCHED));
    }

    @GetMapping("/{eventId}/status")
    public ResponseEntity<ResponseWrapper<String>> getStatus(@PathVariable UUID eventId) {
        log.info("EventController.getStatus eventId ->{}", eventId);
        String status = eventService.statusCheckOfAnEvent(eventId);
        log.info("EventController.getStatus eventId ->{}, status ->{}", eventId, status);
        ResponseWrapper<String> response = ResponseWrapper.success(status, SUCCESSFULLY_FETCHED);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<ResponseWrapper<List<EventResponseDto>>> getAllUserEvents(@PathVariable UUID userId) {
        log.info("EventController.getAllUserEvents userId ->{}", userId);
        List<EventResponseDto> events = eventService.getAllEventsForUser(userId);
        log.info("EventController.getAllUserEvents events.size ->{}", events.size());
        ResponseWrapper<List<EventResponseDto>> response = ResponseWrapper.success(events, SUCCESSFULLY_FETCHED);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/details")
    public ResponseEntity<ResponseWrapper<EventDetailsWithAttendeeCountResponseDto>> getEventWithAttendees(@PathVariable UUID eventId) {
        log.info("EventController.getEventWithAttendees eventId ->{}", eventId);
        EventDetailsWithAttendeeCountResponseDto eventWithAttendeeCount = eventService.getEventWithAttendeeCount(eventId);
        ResponseWrapper<EventDetailsWithAttendeeCountResponseDto> response = ResponseWrapper.success(eventWithAttendeeCount, SUCCESSFULLY_FETCHED);
        log.info("EventController.getEventWithAttendees res -> eventId {}, title {}, description {}", eventId, eventWithAttendeeCount.getTitle(), eventWithAttendeeCount.getDescription());
        return ResponseEntity.ok(response);
    }

}
