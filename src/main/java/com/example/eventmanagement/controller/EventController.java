package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventFilterRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.*;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Visibility;
//import com.example.eventmanagement.rabbitmq.EventProducer;
import com.example.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final EventDetailsModelAssembler eventDetailsModelAssembler;
    private final EventModelAssembler eventModelAssembler;

//    private final EventProducer eventProducer;


    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EventResponseDto>>> getEventsWithFiltering(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("EventController.getEventsWithFiltering req-> location {}, visibility {}, date {}", location , null!=visibility?visibility.toString():null, date);

        EventFilterRequestDto filter = new EventFilterRequestDto(date, null!=visibility?visibility.toString():null, location);
        List<EventResponseDto> result = eventService.getEventsWithFiltering(filter);

        log.info("EventController.getEventsWithFiltering res-> success");
        List<EntityModel<EventResponseDto>> eventModels = result.stream()
                .map(eventModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<EventResponseDto>> collectionModel = CollectionModel.of(eventModels,
                linkTo(methodOn(EventController.class).getEventsWithFiltering(location, visibility, date)).withSelfRel());

        return ResponseEntity.ok(collectionModel);

    }

    @PostMapping
    public ResponseEntity<EntityModel<EventResponseDto>> createEvent(@RequestBody @Valid EventCreateRequestDto requestDto) throws IOException {

        log.info("EventController.createEvent req-> title {}, description {}", requestDto.getTitle(), requestDto.getDescription());

        EventResponseDto responseDto = eventService.saveEvent(requestDto);

        log.info("EventController.createEvent res->id {}", responseDto.getId());

        EntityModel<EventResponseDto> model = eventModelAssembler.toModel(responseDto);

        return ResponseEntity
                .created(linkTo(methodOn(EventController.class).getEventWithAttendees(responseDto.getId())).toUri())
                .body(model);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<EntityModel<EventResponseDto>> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateRequestDto requestDto) {

        log.info("EventController.updateEvent req ->{}", requestDto.toString());

        EventResponseDto eventResponseDto = eventService.updateEvent(eventId, requestDto);

        log.info("EventController.updateEvent updated successfully eventId: {}",eventId);

        EntityModel<EventResponseDto> model = eventModelAssembler.toModel(eventResponseDto);
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isHost(#eventId, principal)")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID eventId) {

        log.info("EventController.deleteEvent eventId ->{}", eventId);

        eventService.updateEventStatus(eventId, EventStatus.DELETED.name());

        log.info("EventController.deleteEvent eventId ->{} success", eventId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PagedModel<EntityModel<EventResponseDto>>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<EventResponseDto> pagedResourcesAssembler) {

        log.info("EventController.getUpcomingEvents page ->{}, size ->{}", page, size);

        Page<EventResponseDto> eventResponseDtos = eventService.listUpcomingEvents(page, size);
        PagedModel<EntityModel<EventResponseDto>> model = pagedResourcesAssembler.toModel(eventResponseDtos, eventModelAssembler);

        log.info("EventController.getUpcomingEvents tot elements ->{}", eventResponseDtos.getTotalElements());

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{eventId}/status")
    public ResponseEntity<EntityModel<EventStatusResponse>> getStatus(@PathVariable UUID eventId) {
        log.info("EventController.getStatus eventId ->{}", eventId);

        String status = eventService.statusCheckOfAnEvent(eventId);

        log.info("EventController.getStatus eventId ->{}, status ->{}", eventId, status);

        EventStatusResponse body = new EventStatusResponse(status);

        EntityModel<EventStatusResponse> model = EntityModel.of(
                body,
                linkTo(methodOn(EventController.class).getStatus(eventId)).withSelfRel(),
                linkTo(methodOn(EventController.class).getEventWithAttendees(eventId)).withRel("eventDetails")
        );

        return ResponseEntity.ok(model);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<CollectionModel<EntityModel<EventResponseDto>>> getAllUserEvents(@PathVariable UUID userId) {
        log.info("EventController.getAllUserEvents userId ->{}", userId);

        List<EventResponseDto> events = eventService.getAllEventsForUser(userId);

        log.info("EventController.getAllUserEvents events.size ->{}", events.size());
        List<EntityModel<EventResponseDto>> eventModels = events.stream()
                .map(eventModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<EventResponseDto>> collectionModel = CollectionModel.of(eventModels,
                linkTo(methodOn(EventController.class).getAllUserEvents(userId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);

    }

    @GetMapping("/{eventId}/details")
    public ResponseEntity<EntityModel<EventDetailsWithAttendeeCountResponseDto>> getEventWithAttendees(@PathVariable UUID eventId) {
        log.info("EventController.getEventWithAttendees eventId ->{}", eventId);

        EventDetailsWithAttendeeCountResponseDto eventWithAttendeeCount = eventService.getEventWithAttendeeCount(eventId);
        EntityModel<EventDetailsWithAttendeeCountResponseDto> model = eventDetailsModelAssembler.toModel(eventWithAttendeeCount);

        log.info("EventController.getEventWithAttendees res -> eventId {}, title {}, description {}", eventId, eventWithAttendeeCount.getTitle(), eventWithAttendeeCount.getDescription());

        return ResponseEntity.ok(model);
    }

}
