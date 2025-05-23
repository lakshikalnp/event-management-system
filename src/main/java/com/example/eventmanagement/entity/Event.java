package com.example.eventmanagement.entity;

import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Event {

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

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }


}
