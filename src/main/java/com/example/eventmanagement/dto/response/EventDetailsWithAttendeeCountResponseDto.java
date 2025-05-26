package com.example.eventmanagement.dto.response;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDetailsWithAttendeeCountResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String location;
    private String visibility;
    private String status;
    private long attendeeCount;

    public static EventDetailsWithAttendeeCountResponseDto fromEntity(Event event, long attendeeCount) {

        return EventDetailsWithAttendeeCountResponseDto.builder()
               .attendeeCount(attendeeCount)
               .endTime(null!=event.getEndTime()?event.getEndTime().format(AppConstants.DATE_TIME_FORMATTER):null)
                .startTime(null!=event.getStartTime()?event.getStartTime().format(AppConstants.DATE_TIME_FORMATTER):null)
                .description(event.getDescription())
                .id(event.getId())
                .location(event.getLocation())
                .status(event.getStatus().name())
                .title(event.getTitle())
                .visibility(event.getVisibility().name())
                .build();

    }
}
