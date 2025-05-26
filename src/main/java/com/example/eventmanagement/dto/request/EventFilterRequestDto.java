package com.example.eventmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterRequestDto {

    private LocalDate date;
    private String visibility;
    private String location;
}
