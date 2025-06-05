package com.example.eventmanagement.controller;

import com.example.eventmanagement.WithMockJwtUser;
import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.entity.Attendance;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.EventStatus;
import com.example.eventmanagement.enumeration.Role;
import com.example.eventmanagement.enumeration.Status;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.repository.AttendanceRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private UUID userId;

    private User testUser;

    private UUID eventId;

    private UUID event2Id;

    @BeforeAll
    void setup() {
        // Initialize test data
        User user = new User();
        user.setName("Lakshika");
        user.setEmail("lakshika1@gmail.com");
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
        event.setStartTime(ZonedDateTime.now().minusDays(3));
        event.setEndTime(ZonedDateTime.now().minusDays(2));
        event.setLocation("Zoom");
        event.setVisibility(Visibility.PUBLIC);
        event.setStatus(EventStatus.ACTIVE);
        event = eventRepository.saveAndFlush(event);
        eventId = event.getId();

         // Create a user who is the event host (someone else)
        User user2 = new User();
        user2.setName("Lakshika");
        user2.setEmail("lakshika2@gmail.com");
        user2.setPassword("123");
        user2.setRole(Role.USER);
        userRepository.saveAndFlush(user2);

        // Create a future event
        Event event2 = new Event();
        event2.setTitle("Future Event");
        event2.setDescription("This is a future event.");
        event2.setStartTime(ZonedDateTime.now().plusDays(1));
        event2.setEndTime(ZonedDateTime.now().plusDays(2));
        event2.setLocation("Test Location");
        event2.setVisibility(Visibility.PRIVATE);
        event2.setStatus(EventStatus.ACTIVE);
        event2.setHost(testUser);

        event2 = eventRepository.saveAndFlush(event2);
        event2Id = event2.getId();

        Attendance attendance = new Attendance();
        attendance.setEvent(event2);
        attendance.setUser(testUser);
        attendance.setStatus(Status.GOING);
        attendance.setRespondedAt(ZonedDateTime.now());
        attendanceRepository.saveAndFlush(attendance);
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void createEvent_success() throws Exception {
        EventCreateRequestDto requestDto = new EventCreateRequestDto();
        requestDto.setTitle("Spring Boot Meetup");
        requestDto.setDescription("Learn Spring Boot in depth.");
        requestDto.setUserId(userId);
        requestDto.setStartTime(ZonedDateTime.now().plusDays(1));
        requestDto.setEndTime(ZonedDateTime.now().plusDays(2));
        requestDto.setLocation("Virtual");
        requestDto.setVisibility(Visibility.PUBLIC);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        assertThat(jsonResponse).contains("Event successfully added with id:");
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void createEvent_validationFails() throws Exception {
        // Arrange
        EventCreateRequestDto requestDto = new EventCreateRequestDto();
        requestDto.setTitle("");  // Invalid
        requestDto.setDescription("");  // Invalid
        requestDto.setUserId(null);  // Invalid
        requestDto.setStartTime(null);  // Invalid
        requestDto.setEndTime(null);  // Invalid
        requestDto.setLocation("");  // Invalid

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();


        System.out.println("Validation Response: " + jsonResponse);

        // Deserialize to get nested "errors" list
        Map<String, Object> responseWrapper = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        List<String> errors = (List<String>) responseWrapper.get("data");


        // Assert individual validation messages
        assertThat(errors).hasSize(6).contains(
                "location is required",
                "startTime is required",
                "endTime is required",
                "description is required",
                "userId is required",
                "title required"
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
        requestDto.setVisibility(Visibility.PUBLIC);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockJwtUser(username = "lakshika1@gmail.com", roles = {"USER"})
    void updateEvent_asHost_shouldSucceed() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility(Visibility.PUBLIC);

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        assertThat(jsonResponse).contains("Event successfully updated with id:");
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "ADMIN")
    void updateEvent_asRoleAdmin_shouldSucceed() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility(Visibility.PUBLIC);

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        assertThat(jsonResponse).contains("Event successfully updated with id:");
    }

    @Test
    @WithMockJwtUser(username = "lakshika2@gmail.com", roles = {"USER"})
    void updateEvent_asRoleUser_shouldForbidden() throws Exception {
        EventCreateRequestDto dto = new EventCreateRequestDto();
        dto.setTitle("Updated Event");
        dto.setDescription("Updated Description");
        dto.setUserId(null); // Not needed in update
        dto.setStartTime(ZonedDateTime.now().plusDays(3));
        dto.setEndTime(ZonedDateTime.now().plusDays(4));
        dto.setLocation("Google Meet");
        dto.setVisibility(Visibility.PUBLIC);

        mockMvc.perform(put("/api/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("You don't have permission to access this resource")));
    }



    @Test
    @WithMockJwtUser(username = "lakshika1@gmail.com", roles = {"USER"})
    void deleteEvent_asHost_shouldSucceed() throws Exception {

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/events/" + eventId))
                .andExpect(status().isOk())
                .andReturn();


        String jsonResponse = mvcResult.getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        String actualMessage = (String) responseMap.get("message");
        String expectedMessage = "Event successfully deleted with id: " + eventId;

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "ADMIN")
    void deleteEvent_asRoleAdmin_shouldSucceed() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/events/" + eventId))
                .andExpect(status().isOk())
                .andReturn();


        String jsonResponse = mvcResult.getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        String actualMessage = (String) responseMap.get("message");
        String expectedMessage = "Event successfully deleted with id: " + eventId;

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @WithMockJwtUser(username = "lakshika2@gmail.com", roles = {"USER"})
    void deleteEvent_asRoleUser_shouldForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/events/" + eventId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("You don't have permission to access this resource")))
                .andReturn();

    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void getUpcomingEvents_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/events/upcoming?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Future Event"));
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void getStatus_shouldReturn200WithCorrectStatus() throws Exception {
        mockMvc.perform(get("/api/v1/events/" + eventId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Successfully fetched"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void getStatus_shouldReturn404WhenEventNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/events/" + invalidId + "/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void shouldReturnAllHostedAndAttendingEvents() throws Exception {
        mockMvc.perform(get("/api/v1/events/user/{userId}/all", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2)) // adjust based on actual count
                .andExpect(jsonPath("$.data[0].title").exists())
                .andExpect(jsonPath("$.data[1].title").exists());
    }

    @Test
    @WithMockUser(username = "lakshika1@gmail.com", roles = "USER")
    void getEventWithAttendeeCount_returnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/events/" + event2Id + "/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Future Event"))
                .andExpect(jsonPath("$.data.attendeeCount").value(1));
    }

    @Test
    @WithMockJwtUser(username = "lakshika1@gmail.com", roles = {"USER"})
    void shouldReturnFilteredEvents_ForHostUser_withLocation() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("location", "Test Location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Future Event"))
                .andExpect(jsonPath("$.data[0].location").value("Test Location"));
    }

    @Test
    @WithMockJwtUser(username = "lakshika3@gmail.com", roles = {"USER"})
    void shouldReturnFilteredEvents_ForNotHostUser_withPublicVisibility() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Original Event"))
                .andExpect(jsonPath("$.data[0].location").value("Zoom"));
    }

    @Test
    @WithMockJwtUser(username = "lakshika1@gmail.com", roles = {"USER"})
    void shouldReturnFilteredEvents_ForHostUser_withPrivateVisibility() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("visibility", "PRIVATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Future Event"))
                .andExpect(jsonPath("$.data[0].location").value("Test Location"));
    }

    @Test
    @WithMockJwtUser(username = "lakshika2@gmail.com", roles = {"USER"})
    void shouldReturnFilteredEvents_ForNotHostUser_withPrivateVisibility() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("visibility", "PRIVATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithMockJwtUser(username = "lakshika1@gmail.com", roles = {"USER"})
    void shouldReturnFilteredEvents_ForHostUser_dateBetweenToday() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("date", String.valueOf(LocalDate.now(ZoneId.of("UTC")).plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}