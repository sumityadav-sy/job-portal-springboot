package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.ApplicationResponseDTO;
import com.sumit.jobportal.entity.ApplicationStatus;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.InvalidInputException;
import com.sumit.jobportal.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    private ApplicationResponseDTO testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new ApplicationResponseDTO(
                1, "APPLIED",
                "Sumit", "sumit@gmail.com",
                1, "Backend Developer", "TechCorp",
                "Anita HR"
        );
    }

    // ═══════════════════════════════════════════════════
    // POST /applications — JOB_SEEKER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("POST /applications → 401 when no token")
    void applyForJob_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post("/applications")
                        .param("userId", "1")
                        .param("jobId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /applications → 403 when RECRUITER tries to apply")
    @WithMockUser(roles = "RECRUITER")
    void applyForJob_shouldReturn403_whenRecruiterApplies() throws Exception {
        mockMvc.perform(post("/applications")
                        .param("userId", "2")
                        .param("jobId", "1"))
                .andExpect(status().isForbidden());

        // @PreAuthorize blocks before service — service never called
        verify(applicationService, never()).applyForJob(anyInt(), anyInt());
    }

    @Test
    @DisplayName("POST /applications → 200 OK when JOB_SEEKER applies")
    @WithMockUser(roles = "JOB_SEEKER")
    void applyForJob_shouldReturn200_whenJobSeekerApplies() throws Exception {
        when(applicationService.applyForJob(1, 1)).thenReturn(testResponse);

        mockMvc.perform(post("/applications")
                        .param("userId", "1")
                        .param("jobId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantName").value("Sumit"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));
    }

    @Test
    @DisplayName("POST /applications → 409 when already applied")
    @WithMockUser(roles = "JOB_SEEKER")
    void applyForJob_shouldReturn409_whenDuplicate() throws Exception {
        when(applicationService.applyForJob(anyInt(), anyInt()))
                .thenThrow(new DuplicateResourceException("Already applied for this job"));

        mockMvc.perform(post("/applications")
                        .param("userId", "1")
                        .param("jobId", "1"))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════════════
    // PUT /applications/{id}/status — RECRUITER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("PUT /applications/{id}/status → 401 when no token")
    void updateStatus_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(put("/applications/1/status")
                        .param("recruiterId", "2")
                        .param("status", "REVIEWED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /applications/{id}/status → 403 when JOB_SEEKER updates status")
    @WithMockUser(roles = "JOB_SEEKER")
    void updateStatus_shouldReturn403_whenJobSeekerUpdates() throws Exception {
        mockMvc.perform(put("/applications/1/status")
                        .param("recruiterId", "1")
                        .param("status", "REVIEWED"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).updateStatus(anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("PUT /applications/{id}/status → 200 OK when RECRUITER updates")
    @WithMockUser(roles = "RECRUITER")
    void updateStatus_shouldReturn200_whenRecruiterUpdates() throws Exception {
        ApplicationResponseDTO reviewed = new ApplicationResponseDTO(
                1, "REVIEWED", "Sumit", "sumit@gmail.com",
                1, "Backend Developer", "TechCorp", "Anita HR");

        when(applicationService.updateStatus(1, 2, ApplicationStatus.REVIEWED))
                .thenReturn(reviewed);

        mockMvc.perform(put("/applications/1/status")
                        .param("recruiterId", "2")
                        .param("status", "REVIEWED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"));
    }

    @Test
    @DisplayName("PUT /applications/{id}/status → 400 when invalid transition")
    @WithMockUser(roles = "RECRUITER")
    void updateStatus_shouldReturn400_whenInvalidTransition() throws Exception {
        when(applicationService.updateStatus(anyInt(), anyInt(), any()))
                .thenThrow(new InvalidInputException("Invalid status transition"));

        mockMvc.perform(put("/applications/1/status")
                        .param("recruiterId", "2")
                        .param("status", "ACCEPTED"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════════════
    // GET /applications/user/{userId} — JOB_SEEKER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /applications/user/{id} → 401 when no token")
    void getByUser_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/applications/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /applications/user/{id} → 403 when RECRUITER accesses")
    @WithMockUser(roles = "RECRUITER")
    void getByUser_shouldReturn403_whenRecruiterAccesses() throws Exception {
        mockMvc.perform(get("/applications/user/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /applications/user/{id} → 200 OK when JOB_SEEKER accesses")
    @WithMockUser(roles = "JOB_SEEKER")
    void getByUser_shouldReturn200_whenJobSeekerAccesses() throws Exception {
        when(applicationService.getApplicationsByUser(1))
                .thenReturn(List.of(testResponse));

        mockMvc.perform(get("/applications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantName").value("Sumit"));
    }

    // ═══════════════════════════════════════════════════
    // GET /applications/job/{jobId} — RECRUITER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /applications/job/{id} → 403 when JOB_SEEKER accesses")
    @WithMockUser(roles = "JOB_SEEKER")
    void getByJob_shouldReturn403_whenJobSeekerAccesses() throws Exception {
        mockMvc.perform(get("/applications/job/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /applications/job/{id} → 200 OK when RECRUITER accesses")
    @WithMockUser(roles = "RECRUITER")
    void getByJob_shouldReturn200_whenRecruiterAccesses() throws Exception {
        when(applicationService.getApplicationsByJob(1))
                .thenReturn(List.of(testResponse));

        mockMvc.perform(get("/applications/job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobTitle").value("Backend Developer"));
    }

    // ═══════════════════════════════════════════════════
    // DELETE /applications/{id} — JOB_SEEKER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /applications/{id} → 401 when no token")
    void withdraw_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(delete("/applications/1")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /applications/{id} → 403 when RECRUITER withdraws")
    @WithMockUser(roles = "RECRUITER")
    void withdraw_shouldReturn403_whenRecruiterWithdraws() throws Exception {
        mockMvc.perform(delete("/applications/1")
                        .param("userId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /applications/{id} → 200 OK when JOB_SEEKER withdraws own application")
    @WithMockUser(roles = "JOB_SEEKER")
    void withdraw_shouldReturn200_whenJobSeekerWithdraws() throws Exception {
        doNothing().when(applicationService).withdrawApplication(1, 1);

        mockMvc.perform(delete("/applications/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application withdrawn successfully."));
    }

    @Test
    @DisplayName("DELETE /applications/{id} → 400 when application not in APPLIED status")
    @WithMockUser(roles = "JOB_SEEKER")
    void withdraw_shouldReturn400_whenNotAppliedStatus() throws Exception {
        doThrow(new InvalidInputException("Can only withdraw APPLIED applications"))
                .when(applicationService).withdrawApplication(anyInt(), anyInt());

        mockMvc.perform(delete("/applications/1")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }
}