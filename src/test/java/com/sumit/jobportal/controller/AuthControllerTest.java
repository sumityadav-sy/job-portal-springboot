package com.sumit.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.jobportal.dto.LoginRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock AuthenticationManager so we don't need a real DB for login tests
    @MockitoBean
    private AuthenticationManager authenticationManager;

    // ─── TEST 1: Login — Wrong Credentials → 500 (caught by generic handler) ──

    @Test
    @DisplayName("POST /auth/login → 401 when credentials are wrong")
    void login_shouldFail_whenBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequestDTO badRequest = new LoginRequestDTO();
        badRequest.setEmail("wrong@email.com");
        badRequest.setPassword("wrongpass");

        // Your security config correctly returns 401 for bad credentials
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized()) // 401 — correct behavior
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password")); // verify the message too
    }

    // ─── TEST 2: Login — Blank Email → 400 Validation ─────────────────────────

    @Test
    @DisplayName("POST /auth/login → 400 when email is blank")
    void login_shouldReturn400_whenEmailBlank() throws Exception {
        LoginRequestDTO badRequest = new LoginRequestDTO();
        badRequest.setEmail("");
        badRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    // ─── TEST 3: Login — Blank Password → 400 Validation ─────────────────────

    @Test
    @DisplayName("POST /auth/login → 400 when password is blank")
    void login_shouldReturn400_whenPasswordBlank() throws Exception {
        LoginRequestDTO badRequest = new LoginRequestDTO();
        badRequest.setEmail("sumit@gmail.com");
        badRequest.setPassword("");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }
}