package com.sumit.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.jobportal.dto.UserRequestDTO;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest loads the COMPLETE Spring application context
// This means SecurityConfig, JwtFilter, all controllers, all beans
// It's heavier than unit tests but tests the real wiring

@SpringBootTest
@AutoConfigureMockMvc  // injects MockMvc configured with your full security setup
class UserControllerTest {

    // MockMvc is our HTTP simulator — we perform requests through it
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper converts Java objects → JSON strings
    // We need this to send request bodies as JSON
    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean replaces the real UserService in the Spring context
    // The controller gets this mock injected instead of the real service
    // This way we test the controller layer only — not the service logic
    // (we already tested service logic in Part 1)
    @MockitoBean
    private UserService userService;

    private UserResponseDTO testResponse;
    private UserRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testResponse = new UserResponseDTO(1, "Sumit", "sumit@gmail.com", "JOB_SEEKER");

        testRequest = new UserRequestDTO();
        testRequest.setName("Sumit");
        testRequest.setEmail("sumit@gmail.com");
        testRequest.setPassword("password123");
        testRequest.setRole(Role.JOB_SEEKER);
    }

    // ─── TEST 1: Register User — Happy Path ──────────────────────────────────

    @Test
    @DisplayName("POST /api/users/register → 201 Created with user DTO")
    void register_shouldReturn201_whenValidRequest() throws Exception {

        // ARRANGE — mock the service
        when(userService.registerUser(any())).thenReturn(testResponse);

        // ACT + ASSERT
        mockMvc.perform(
                post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        // objectMapper.writeValueAsString() converts UserRequestDTO → JSON string
                        // {"name":"Sumit","email":"sumit@gmail.com","password":"password123","role":"JOB_SEEKER"}
                        .content(objectMapper.writeValueAsString(testRequest))
        )
        .andExpect(status().isCreated())                      // HTTP 201
        .andExpect(jsonPath("$.name").value("Sumit"))         // JSON body check
        .andExpect(jsonPath("$.email").value("sumit@gmail.com"))
        .andExpect(jsonPath("$.role").value("JOB_SEEKER"));
        // jsonPath("$.name") = "in the JSON response body, find the field called 'name'"
    }

    // ─── TEST 2: Register User — Duplicate Email → 409 ───────────────────────

    @Test
    @DisplayName("POST /api/users/register → 409 Conflict when email already exists")
    void register_shouldReturn409_whenEmailDuplicate() throws Exception {

        // ARRANGE — service throws DuplicateResourceException
        when(userService.registerUser(any()))
                .thenThrow(new DuplicateResourceException("Email already registered"));

        // ACT + ASSERT
        mockMvc.perform(
                post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest))
        )
        .andExpect(status().isConflict());  // HTTP 409
        // GlobalExceptionHandler catches DuplicateResourceException → 409
    }

    // ─── TEST 3: Register User — Validation Failure → 400 ────────────────────

    @Test
    @DisplayName("POST /api/users/register → 400 Bad Request when name is blank")
    void register_shouldReturn400_whenNameIsBlank() throws Exception {

        // ARRANGE — invalid request: name is empty
        testRequest.setName("");

        // ACT + ASSERT
        // @Valid on the controller catches this before service is even called
        mockMvc.perform(
                post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest))
        )
        .andExpect(status().isBadRequest());  // HTTP 400
    }

    // ─── TEST 4: Get All Users — No Token → 401 ──────────────────────────────

    @Test
    @DisplayName("GET /api/users → 401 Unauthorized when no token provided")
    void getAllUsers_shouldReturn401_whenNoToken() throws Exception {

        // No Authorization header — Spring Security blocks before controller
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());  // HTTP 401
    }

    // ─── TEST 5: Get All Users — With Valid Token → 200 ──────────────────────

    @Test
    @DisplayName("GET /api/users → 200 OK when authenticated")
    // @WithMockUser simulates a logged-in user with ROLE_JOB_SEEKER
    // It bypasses JWT validation — Spring Security treats this as authenticated
    // roles= must match what your @PreAuthorize expects
    @WithMockUser(roles = "JOB_SEEKER")
    void getAllUsers_shouldReturn200_whenAuthenticated() throws Exception {

        // ARRANGE
        when(userService.getAllUsers()).thenReturn(List.of(testResponse));

        // ACT + ASSERT
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())                 // HTTP 200
                .andExpect(jsonPath("$[0].name").value("Sumit"));
                // $[0] = first element of the JSON array
    }

    // ─── TEST 6: Get User By ID — Not Found → 404 ────────────────────────────

    @Test
    @DisplayName("GET /api/users/{id} → 404 Not Found when user missing")
    @WithMockUser(roles = "RECRUITER")
    void getUserById_shouldReturn404_whenUserNotFound() throws Exception {

        // ARRANGE — service throws ResourceNotFoundException
        when(userService.getUserById(99))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())  // HTTP 404
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: 99"));
                // GlobalExceptionHandler builds ApiError with this message field
    }
}