package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventCreateResponseDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.exception.ResourceNotFoundException;
import com.example.eventmanagement.mapstruct.MappingContext;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final MappingContext mappingContext;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventCreateResponseDto saveEvent(EventCreateRequestDto eventCreateRequestDto) {
        log.info("saveEvent called");
        Event event = mappingContext
                .getStrategy(EventCreateRequestDto.class, Event.class)
                .map(eventCreateRequestDto);

        event = eventRepository.saveAndFlush(event);

        log.info("Event added successfully");
        return EventCreateResponseDto.eventToDto(event);
    }

    public EventCreateResponseDto updateEvent(UUID eventId, EventUpdateRequestDto requestDto) {
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

        // Save updated entity
        eventRepository.saveAndFlush(existingEvent);

        log.info("Event updated successfully.");
        return EventCreateResponseDto.eventToDto(existingEvent);
    }


}
