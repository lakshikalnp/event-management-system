package com.example.eventmanagement.security;

import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("eventSecurity")
@RequiredArgsConstructor
public class EventSecurity {

    private final EventRepository eventRepository;

    public boolean isHost(UUID eventId, User user) {
        String email = user.getEmail();
        return eventRepository.findById(eventId)
                .map(event -> event.getHost().getEmail().equals(email))
                .orElse(false);
    }
}
