package com.example.eventmanagement.entity;

import com.example.eventmanagement.entity.ids.AttendanceId;
import com.example.eventmanagement.enumeration.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@IdClass(AttendanceId.class)
@Data
@NoArgsConstructor
@Entity
public class Attendance {

    @Id
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private Event event;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;

    private ZonedDateTime respondedAt;
}
