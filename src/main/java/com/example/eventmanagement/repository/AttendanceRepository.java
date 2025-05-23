package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.id = :eventId AND a.status <> 'DECLINED' ")
    long countAttendeesByEventIdAndStatus(@Param("eventId") UUID eventId);
}
