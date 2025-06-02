package com.example.eventmanagement.dto.response;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.util.AppConstants;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class EventResponseDto implements Serializable {

    private UUID id;

    private String title;

    private String description;

    private UUID host;

    private String startTime;

    private String endTime;

    private String location;

    private Visibility visibility;

    private String createdAt;

    private String updatedAt;

    private String status;

    public static EventResponseDto eventToDto (Event event) {
        return EventResponseDto.builder()
                .createdAt(null!=event.getCreatedAt()?event.getCreatedAt().format(AppConstants.DATE_TIME_FORMATTER):null)
                .updatedAt(null!=event.getUpdatedAt()?event.getUpdatedAt().format(AppConstants.DATE_TIME_FORMATTER):null)
                .startTime(null!=event.getStartTime()?event.getStartTime().format(AppConstants.DATE_TIME_FORMATTER):null)
                .endTime(null!=event.getEndTime()?event.getEndTime().format(AppConstants.DATE_TIME_FORMATTER):null)
                .description(event.getDescription())
                .host(event.getHost().getId())
                .id(event.getId())
                .location(event.getLocation())
                .visibility(event.getVisibility())
                .title(event.getTitle())
                .status(event.getStatus().name())
                .build();
    }
}
