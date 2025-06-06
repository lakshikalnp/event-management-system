package com.example.eventmanagement.dto.response;

import com.example.eventmanagement.controller.EventController;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
public class EventModelAssembler implements RepresentationModelAssembler<EventResponseDto, EntityModel<EventResponseDto>> {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public EntityModel<EventResponseDto> toModel(EventResponseDto dto) {
        UUID eventId = dto.getId();

        EntityModel<EventResponseDto> model = EntityModel.of(dto,
                linkTo(methodOn(EventController.class).getEventWithAttendees(eventId)).withSelfRel(),
                linkTo(methodOn(EventController.class).getStatus(eventId)).withRel("status")
        );


        // Access authentication details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            // Conditionally add links based on role
            if (isAdmin || isHost(dto, user.getEmail())) {
                model.add(linkTo(methodOn(EventController.class).deleteEvent(eventId)).withRel("delete").withType("DELETE"));
                model.add(linkTo(methodOn(EventController.class).updateEvent(eventId, new EventUpdateRequestDto())).withRel("update").withType("PUT"));
            }
        }

        return model;
    }

    private boolean isHost(EventResponseDto dto, String email) {
        // Implement your logic here to compare the event's host with the current user
        User byEmail = userRepository.findByEmail(email).orElse(null);
        assert byEmail != null;
        return dto.getHost().equals(byEmail.getId());
    }
}
