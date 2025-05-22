package com.example.eventmanagement.security;

import com.example.eventmanagement.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("eventSecurity")
@RequiredArgsConstructor
public class EventSecurity {

    private final EventRepository eventRepository;

    public boolean isHost(UUID eventId, UserDetails userDetails) {
        String email = userDetails.getUsername();
        return eventRepository.findById(eventId)
                .map(event -> event.getHost().getEmail().equals(email))
                .orElse(false);
    }
}
