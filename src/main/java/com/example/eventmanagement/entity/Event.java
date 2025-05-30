package com.example.eventmanagement.entity;

import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Event extends Common {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "host_id", referencedColumnName = "id",updatable = false)
    private User host;

    private ZonedDateTime startTime;

    private ZonedDateTime endTime;

    private String location;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

}
