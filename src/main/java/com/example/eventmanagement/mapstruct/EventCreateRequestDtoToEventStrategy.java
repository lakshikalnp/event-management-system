package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCreateRequestDtoToEventStrategy implements MappingStrategy<EventCreateRequestDto, Event>{
    private final EventMapper eventMapper;

    @Override
    public Event map(EventCreateRequestDto source) {
        return eventMapper.toEvent(source);
    }
}
