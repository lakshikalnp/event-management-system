package com.example.eventmanagement.dto.response;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.util.AppConstants;
import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;


@Builder
public record EventCreatedNotificationResponseDto(UUID id, String title, String description,
                                                  com.example.eventmanagement.dto.response.EventCreatedNotificationResponseDto.User host,
                                                  String startTime, String endTime, String location, String visibility,
                                                  String status) implements Serializable {

    public static EventCreatedNotificationResponseDto eventToDto(Event event) {
        return EventCreatedNotificationResponseDto.builder()
                .startTime(null != event.getStartTime() ? event.getStartTime().format(AppConstants.DATE_TIME_FORMATTER) : null)
                .endTime(null != event.getEndTime() ? event.getEndTime().format(AppConstants.DATE_TIME_FORMATTER) : null)
                .description(event.getDescription())
                .host(new User(event.getId(), event.getHost().getName(), event.getHost().getEmail()))
                .id(event.getId())
                .location(event.getLocation())
                .visibility(event.getVisibility().toString())
                .title(event.getTitle())
                .status(event.getStatus().toString())
                .build();
    }

    public record User(UUID id, String name, String email) implements Serializable {

    }
}
