package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enumeration.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository  extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    Page<Event> findByStartTimeAfterAndStatus(ZonedDateTime startTimeAfter, EventStatus status, Pageable page);

    List<Event> findByHostIdAndStatus(UUID hostId, EventStatus status);

    @Query("SELECT a.event FROM Attendance a WHERE a.user.id = :userId And a.event.status = :eventStatus ")
    List<Event> findAttendingEventsByUserId(@Param("userId") UUID userId, @Param("eventStatus") EventStatus eventStatus);

    Optional<Event> findByIdAndStatus(UUID id, EventStatus status);
}
