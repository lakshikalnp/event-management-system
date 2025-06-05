package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventResponseDto;
import com.example.eventmanagement.dto.response.EventDetailsWithAttendeeCountResponseDto;
import com.example.eventmanagement.entity.Attendance;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Role;
import com.example.eventmanagement.enumeration.Status;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.exception.ResourceNotFoundException;
import com.example.eventmanagement.repository.AttendanceRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private UUID userId;

    private UUID eventId;

    private UUID event2Id;



    @BeforeAll
     void setup()  {


        // Initialize test data
        User user = new User();
        user.setName("Lakshika");
        user.setEmail("lakshika1@gmail.com");
        user.setPassword("123");
        user.setRole(Role.USER);

        User testUser = userRepository.saveAndFlush(user);
        userId = testUser.getId();

        // Save an event
        Event event = new Event();
        event.setTitle("Old Event");
        event.setDescription("Old Description");
        event.setHost(testUser);
        event.setStartTime(ZonedDateTime.now().minusDays(3));
        event.setEndTime(ZonedDateTime.now().minusDays(2));
        event.setLocation("Old Location");
        event.setVisibility(Visibility.PUBLIC);
        event.setStatus(EventStatus.ACTIVE);

        event = eventRepository.saveAndFlush(event);
        eventId = event.getId();

        // Create a future event
        Event event2 = new Event();
        event2.setTitle("Future Event");
        event2.setDescription("This is a future event.");
        event2.setStartTime(ZonedDateTime.now().plusDays(1));
        event2.setEndTime(ZonedDateTime.now().plusDays(2));
        event2.setLocation("Test Location");
        event2.setVisibility(Visibility.PRIVATE);
        event.setHost(testUser);
        event2.setStatus(EventStatus.ACTIVE);
        event2.setCreatedAt(ZonedDateTime.now());
        event2.setUpdatedAt(ZonedDateTime.now());
        event2.setHost(testUser);

        event2 = eventRepository.save(event2);
        event2Id = event2.getId();

        Attendance attendance = new Attendance();
        attendance.setEvent(event2);
        attendance.setUser(user);
        attendance.setStatus(Status.GOING);
        attendance.setRespondedAt(ZonedDateTime.now());
        attendanceRepository.saveAndFlush(attendance);
    }

    @Test
    void testSaveAndFindEvent() {
       userRepository.findById(userId);

        // Create and save eventCreateRequestDto
        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto();
        eventCreateRequestDto.setDescription("Birth day party of asendra 3 years old child");
        eventCreateRequestDto.setUserId(userId);
        eventCreateRequestDto.setLocation("Glkissa");
        eventCreateRequestDto.setTitle("asendra Birth day party at december 30,2025");
        eventCreateRequestDto.setVisibility(Visibility.PUBLIC);
        eventCreateRequestDto.setStartTime(ZonedDateTime.of(LocalDate.of(2025,12,30), LocalTime.of(12,0,0), ZoneId.of("UTC")));
        eventCreateRequestDto.setEndTime(ZonedDateTime.of(LocalDate.of(2025,12,30), LocalTime.of(16,0,0), ZoneId.of("UTC")));
        EventResponseDto eventResponseDto = eventService.saveEvent(eventCreateRequestDto);

        // Verify it can be found

        Optional<Event> result = eventRepository.findById(eventResponseDto.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getHost().getId()).isEqualTo(userId);
        assertThat(result.get().getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void updateEvent_shouldUpdateSuccessfully() {
        // Given
        EventUpdateRequestDto dto = new EventUpdateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Updated Location");

        // When
        EventResponseDto updated = eventService.updateEvent(eventId, dto);

        // Then
        Assertions.assertEquals("Updated Event", updated.getTitle());
        Assertions.assertEquals("Updated Description", updated.getDescription());
        Assertions.assertEquals("Updated Location", updated.getLocation());
        Assertions.assertEquals("PUBLIC", updated.getVisibility().name());
    }

    @Test
    void updateEventStatus() {

        EventResponseDto eventResponseDto = eventService.updateEventStatus(eventId, EventStatus.DELETED.name());
        // Then
        Assertions.assertEquals(EventStatus.DELETED.name(), eventResponseDto.getStatus());
    }

    @Test
    void listUpcomingEvents_returnsExpectedResults() {
        Page<EventResponseDto> result = eventService.listUpcomingEvents(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Future Event");
    }

    @Test
    void listUpcomingEvents_changedPageSize_returnsExpectedResults() {
        Page<EventResponseDto> result = eventService.listUpcomingEvents(0, 1);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Future Event");
    }

    @Test
    void statusCheckOfAnEvent_shouldReturnCorrectStatus() {
        String status = eventService.statusCheckOfAnEvent(eventId);
        assertThat(status).isEqualTo("ACTIVE");
    }

    @Test
    void statusCheckOfAnEvent_shouldThrowIfEventNotFound() {
        UUID invalidId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> eventService.statusCheckOfAnEvent(invalidId));
    }

    @Test
    void shouldReturnHostedAndAttendingEvents() {
        List<EventResponseDto> events = eventService.getAllEventsForUser(userId);
        assertThat(events).hasSize(2);
        assertThat(events).extracting(EventResponseDto::getTitle)
                .containsExactlyInAnyOrder("Old Event", "Future Event");
    }

    @Test
    void testGetEventWithAttendeeCount() {
        EventDetailsWithAttendeeCountResponseDto result = eventService.getEventWithAttendeeCount(event2Id);
        assertThat(result.getAttendeeCount()).isEqualTo(1);
        assertThat(result.getTitle()).isEqualTo("Future Event");
    }


}