package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.dto.request.EventUpdateRequestDto;
import com.example.eventmanagement.dto.response.EventCreateResponseDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Role;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private UUID userId;

    private User testUser;

    private UUID eventId;

    @BeforeEach
     void setup() {
        // Initialize test data
        User user = new User();
        user.setName("Lakshika");
        user.setEmail("lakshika@gmail.com");
        user.setPassword("123");
        user.setRole(Role.USER);

        testUser = userRepository.saveAndFlush(user);
        userId = testUser.getId();

        // Save an event
        Event event = new Event();
        event.setTitle("Old Event");
        event.setDescription("Old Description");
        event.setHost(testUser);
        event.setStartTime(ZonedDateTime.now().plusDays(1));
        event.setEndTime(ZonedDateTime.now().plusDays(2));
        event.setLocation("Old Location");
        event.setVisibility(Visibility.PUBLIC);

        event = eventRepository.save(event);
        eventId = event.getId();
    }

    @Test
    public void testSaveAndFindEvent() {
        Optional<User> user = userRepository.findById(userId);

        // Create and save eventCreateRequestDto
        EventCreateRequestDto eventCreateRequestDto = new EventCreateRequestDto();
        eventCreateRequestDto.setDescription("Birth day party of asendra 3 years old child");
        eventCreateRequestDto.setUserId(userId);
        eventCreateRequestDto.setLocation("Glkissa");
        eventCreateRequestDto.setTitle("asendra Birth day party at december 30,2025");
        eventCreateRequestDto.setVisibility(Visibility.PUBLIC.toString());
        eventCreateRequestDto.setStartTime(ZonedDateTime.of(LocalDate.of(2025,12,30), LocalTime.of(12,0,0), ZoneId.of("UTC")));
        eventCreateRequestDto.setEndTime(ZonedDateTime.of(LocalDate.of(2025,12,30), LocalTime.of(16,0,0), ZoneId.of("UTC")));
        EventCreateResponseDto eventCreateResponseDto = eventService.saveEvent(eventCreateRequestDto);

        // Verify it can be found

        Optional<Event> result = eventRepository.findById(eventCreateResponseDto.getId());

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
        EventCreateResponseDto updated = eventService.updateEvent(eventId, dto);

        // Then
        Assertions.assertEquals("Updated Event", updated.getTitle());
        Assertions.assertEquals("Updated Description", updated.getDescription());
        Assertions.assertEquals("Updated Location", updated.getLocation());
        Assertions.assertEquals("PUBLIC", updated.getVisibility().name());
    }
}