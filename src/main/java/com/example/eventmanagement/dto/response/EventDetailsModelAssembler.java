package com.example.eventmanagement.dto.response;

import com.example.eventmanagement.controller.EventController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EventDetailsModelAssembler implements RepresentationModelAssembler<
        EventDetailsWithAttendeeCountResponseDto,
        EntityModel<EventDetailsWithAttendeeCountResponseDto>> {

    @Override
    @NonNull
    public EntityModel<EventDetailsWithAttendeeCountResponseDto> toModel(EventDetailsWithAttendeeCountResponseDto dto) {
        UUID eventId = dto.getId();

        return EntityModel.of(dto,
                linkTo(methodOn(EventController.class).getEventWithAttendees(eventId)).withSelfRel(),
                linkTo(methodOn(EventController.class).getStatus(eventId)).withRel("status")
        );
    }
}
