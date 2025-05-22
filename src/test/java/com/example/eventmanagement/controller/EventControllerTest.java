package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Role;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private UUID userId;

    private User testUser;

    private UUID eventId;

    @BeforeAll
    void setup() {
        // Initialize test data
        User user = new User();
        user.setName("Lakshika");
        user.setEmail("lakshika@gmail.com");
        user.setPassword("123");
        user.setRole(Role.USER);

        testUser = userRepository.saveAndFlush(user);
        userId = testUser.getId();



    }

    @BeforeEach
    void setupEvent() {
        Event event = new Event();
        event.setTitle("Original Event");
        event.setDescription("Original Description");
        event.setHost(testUser);
        event.setStartTime(ZonedDateTime.now().plusDays(1));
        event.setEndTime(ZonedDateTime.now().plusDays(2));
        event.setLocation("Zoom");
        event.setVisibility(Visibility.PUBLIC);

        event = eventRepository.save(event);
        eventId = event.getId();

         // Create a user who is the event host (someone else)
        User user2 = new User();
        user2.setName("Lakshika");
        user2.setEmail("lakshika2@gmail.com");
        user2.setPassword("123");
        user2.setRole(Role.USER);
        userRepository.saveAndFlush(user2);
    }

    @Test
    @WithMockUser(username = "lakshika@gmail.com", roles = "USER")
    void createEvent_success() throws Exception {
        EventCreateRequestDto requestDto = new EventCreateRequestDto();
        requestDto.setTitle("Spring Boot Meetup");
        requestDto.setDescription("Learn Spring Boot in depth.");
        requestDto.setUserId(userId);
        requestDto.setStartTime(ZonedDateTime.now().plusDays(1));
        requestDto.setEndTime(ZonedDateTime.now().plusDays(2));
        requestDto.setLocation("Virtual");
        requestDto.setVisibility("PUBLIC");

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // ✅ Valid in test
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "lakshika@gmail.com", roles = "USER")
    void createEvent_validationFails() throws Exception {
        // Arrange
        EventCreateRequestDto requestDto = new EventCreateRequestDto();
        requestDto.setTitle("");  // Invalid
        requestDto.setDescription("");  // Invalid
        requestDto.setUserId(null);  // Invalid
        requestDto.setStartTime(null);  // Invalid
        requestDto.setEndTime(null);  // Invalid
        requestDto.setLocation("");  // Invalid
        requestDto.setVisibility("UNKNOWN");  // Invalid enum string

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        // Debug/log the raw response if needed
        System.out.println("Validation Response: " + jsonResponse);

        // Deserialize JSON to a map or list depending on your error structure
        Map<String, Object> errorMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        List<String> errors = (List<String>) errorMap.get("errors");  // adjust key as per your error structure

        // Assert individual validation messages
        assertThat(errors).contains(
                "Title must not be blank",
                "Description must not be blank",
                "User ID is required",
                "Start time is required",
                "End time is required",
                "Location must not be blank",
                "Invalid visibility value"
        );
    }

    @Test
    void createEvent_authentication_Failure() throws Exception {
        EventCreateRequestDto requestDto = new EventCreateRequestDto();
        requestDto.setTitle("Spring Boot Meetup");
        requestDto.setDescription("Learn Spring Boot in depth.");
        requestDto.setUserId(userId);
        requestDto.setStartTime(ZonedDateTime.now().plusDays(1));
        requestDto.setEndTime(ZonedDateTime.now().plusDays(2));
        requestDto.setLocation("Virtual");
        requestDto.setVisibility("PUBLIC");

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("lakshika@gmail.com")
    void updateEvent_asHost_shouldSucceed() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility("PUBLIC");

        mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.location").value("Google Meet"));
    }

    @Test
    @WithMockUser(username = "lakshika@gmail.com", roles = "ADMIN")
    void updateEvent_asRoleAdmin_shouldSucceed() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility("PUBLIC");

        mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.location").value("Google Meet"));
    }

    @Test
    @WithMockUser(username = "lakshika2@gmail.com", roles = "USER")
    void updateEvent_asRoleUser_shouldUnAuthorized() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility("PUBLIC");

        mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("You don't have permission to access this resource")));
    }
}