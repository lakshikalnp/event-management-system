package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventFilterRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventCreatedNotificationResponseDto;
import com.example.eventmanagement.dto.response.EventResponseDto;
import com.example.eventmanagement.dto.response.EventDetailsWithAttendeeCountResponseDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.EventType;
import com.example.eventmanagement.enumeration.NotificationType;
import com.example.eventmanagement.exception.ResourceNotFoundException;
import com.example.eventmanagement.mapstruct.MappingContext;
import com.example.eventmanagement.rabbitmq.EventProducer;
import com.example.eventmanagement.repository.AttendanceRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.specification.EventSpecification;
import com.example.eventmanagement.util.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static com.example.eventmanagement.util.AppConstants.NOT_FOUND_EVENT;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final MappingContext mappingContext;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    @CacheEvict(value = "upcomingEvents", allEntries = true)
    public EventResponseDto saveEvent(EventCreateRequestDto eventCreateRequestDto) throws IOException {
        log.info("saveEvent called");
        Event event = mappingContext
                .getStrategy(EventCreateRequestDto.class, Event.class)
                .map(eventCreateRequestDto);
        event.setStatus(EventStatus.ACTIVE);
        event = eventRepository.saveAndFlush(event);

        log.info("Event added successfully");

//        eventProducer.sendEvent(getCreateEventNotificationTemplate(event, "templates/create-event-email.html"), "An event created");
        Message msg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(EventCreatedNotificationResponseDto.eventToDto(event)))
                .setHeader("eventType", EventType.AN_EVENT_CREATED.name())
                .setHeader("notificationType", NotificationType.EMAIL.name())
                .setHeader("title","an_event_created")
                .setHeader(AppConstants.X_RETRY_COUNT, 0)
                .build();
        try {
            eventProducer.sendEvent(msg);
        } catch (Exception e) {
           log.error("Error Occurred while sending notification ", e);
        }


        return EventResponseDto.eventToDto(event);
    }

//    private static String getCreateEventNotificationTemplate(Event event, String tempalteName) throws IOException {
//        // create notification template
//        // Define formatter with pattern yyyy-MM-dd HH:mm z
//        DateTimeFormatter formatter =
//                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z", Locale.getDefault());
//        String template = Files.readString(Path.of(tempalteName));
//        return template
//                .replace("${event.status}", event.getStatus().name())
//                .replace("${event.host.name}", event.getHost().getName())
//                .replace("${event.id}", event.getId().toString())
//                .replace("${event.title}", event.getTitle())
//                .replace("${event.description}", event.getDescription())
//                .replace("${event.host.email}", event.getHost().getEmail())
//                .replace("${event.startTime}", formatter.format(event.getStartTime()))
//                .replace("${event.endTime}", formatter.format(event.getEndTime()))
//                .replace("${event.location}", event.getLocation())
//                .replace("${event.visibility}", event.getVisibility().name());
//    }


    @CachePut(value = "upcomingEvents", key = "#eventId")
    public EventResponseDto updateEvent(UUID eventId, EventUpdateRequestDto requestDto) {
        log.info("Updating event with ID: {}", eventId);

        // Fetch existing event from the database
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (null!=requestDto.getTitle())
            existingEvent.setTitle(requestDto.getTitle());
        if (null!=requestDto.getDescription())
            existingEvent.setDescription(requestDto.getDescription());
        if (null!=requestDto.getStartTime())
            existingEvent.setStartTime(requestDto.getStartTime());
        if (null!=requestDto.getEndTime())
            existingEvent.setEndTime(requestDto.getEndTime());
        if (null!=requestDto.getLocation())
            existingEvent.setLocation(requestDto.getLocation());
        if (null!=requestDto.getStatus())
            existingEvent.setStatus(EventStatus.valueOf(requestDto.getStatus()));

        // Save updated entity
        eventRepository.saveAndFlush(existingEvent);

        log.info("Event updated successfully.");
        return EventResponseDto.eventToDto(existingEvent);
    }

    @CachePut(value = "upcomingEvents",  key = "#eventId")
    public EventResponseDto updateEventStatus(UUID eventId, String newStatus) {
        log.info("Updating event status with ID: {} , Status: {}", eventId, newStatus);
        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        Event event = optionalEvent.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_EVENT + eventId));
        event.setStatus(EventStatus.valueOf(newStatus));
        event = eventRepository.saveAndFlush(event);
        log.info("Updating event status with ID: {} , Status: {} success", eventId, newStatus);
        return EventResponseDto.eventToDto(event);
    }

    @Cacheable(value = "upcomingEvents", key = "'page_' + #page + '_size_' + #size")
    public Page<EventResponseDto> listUpcomingEvents(int page, int size) {
        log.info("List up coming events page: {} , size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<Event> eventsPage = eventRepository.findByStartTimeAfterAndStatus(ZonedDateTime.now(ZoneOffset.UTC),EventStatus.ACTIVE, pageable);
        log.info("List up coming events success tot no of elements: {}", eventsPage.getTotalElements());
        return eventsPage.map(EventResponseDto::eventToDto);
    }

    public String statusCheckOfAnEvent(UUID eventId) {
        log.info("Check status of an event eventId: {}", eventId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_EVENT+ eventId));
        log.info("Check status of an event status: {} success", event.getStatus().name());
        return event.getStatus().name();
    }

    public List<EventResponseDto> getAllEventsForUser(UUID userId) {
        log.info("List all events of userId: {}", userId);
        List<Event> hosted = eventRepository.findByHostIdAndStatus(userId, EventStatus.ACTIVE);
        List<Event> attending = eventRepository.findAttendingEventsByUserId(userId, EventStatus.ACTIVE);

        // Avoid duplication if the user is host and attendee of the same event
        Set<Event> combined = new HashSet<>();
        combined.addAll(hosted);
        combined.addAll(attending);
        log.info("List all events of userId: {} List: {} success", userId, combined);
        return combined.stream()
                .map(EventResponseDto::eventToDto)
                .toList();
    }

    public EventDetailsWithAttendeeCountResponseDto getEventWithAttendeeCount(UUID eventId) {
        log.info("get event with attendee count -> eventId: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_EVENT + eventId));
        long count = attendanceRepository.countAttendeesByEventIdAndStatus(eventId);
        log.info("get event with attendee count -> eventId: {}, count: {} success", eventId, count);
        return EventDetailsWithAttendeeCountResponseDto.fromEntity(event, count);
    }

    public List<EventResponseDto> getEventsWithFiltering (EventFilterRequestDto filter) {
        log.info("get event list with filtering -> filter req: {}", filter.toString());
        Specification<Event> spec = Specification
                .where(EventSpecification.hasLocation(filter.getLocation()))
                .and(EventSpecification.hasVisibility(filter.getVisibility()))
                .and(EventSpecification.betweenDate(filter.getDate()))
                .and(EventSpecification.hasAccessToEvent())
                ;
        List<Event> dataList = eventRepository.findAll(spec);
        log.info("get event list with filtering -> filter res size: {}", dataList.size());
        return dataList.stream().map(EventResponseDto::eventToDto).toList();
    }

}
