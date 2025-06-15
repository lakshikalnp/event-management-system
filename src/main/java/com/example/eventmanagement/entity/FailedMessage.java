package com.example.eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
public class FailedMessage extends Common {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String errorDescription;

    @Column(nullable = false)
    private Integer retryAttempts = 0;

    @Column(nullable = false)
    private Integer maxRetryAttempts = 0;

    @Column(columnDefinition = "TEXT")
    private String payLoad;

    @Column(nullable = false)
    private boolean sendToDlq = false;
}
