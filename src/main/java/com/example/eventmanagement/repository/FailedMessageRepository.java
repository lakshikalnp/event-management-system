package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.FailedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FailedMessageRepository  extends JpaRepository<FailedMessage, UUID> {
    List<FailedMessage> findBySendToDlqFalse();

}
