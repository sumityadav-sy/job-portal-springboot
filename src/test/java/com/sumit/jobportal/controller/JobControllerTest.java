package com.sumit.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.jobportal.dto.JobRequestDTO;
import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.service.JobService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    private JobResponseDTO testJobResponse;
    private JobRequestDTO testJobRequest;

    @BeforeEach
    void setUp() {
        testJobResponse = new JobResponseDTO(
                1, "Backend Developer", "Spring Boot role",
                "Bangalore", 80000,
                "Anita HR", "anita@company.com",
                LocalDateTime.now());

        testJobRequest = new JobRequestDTO();
        testJobRequest.setTitle("Backend Developer");
        testJobRequest.setCompany("TechCorp");
        testJobRequest.setLocation("Bangalore");
        testJobRequest.setSalary(80000);
        testJobRequest.setDescription("Spring Boot role");
    }

    // ═══════════════════════════════════════════════════
    // POST /jobs — RECRUITER ONLY (@PreAuthorize)
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("POST /jobs → 401 when no token provided")
    void postJob_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(
                post("/jobs")
                        .param("recruiterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJobRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /jobs → 403 when JOB_SEEKER tries to post")
    // JOB_SEEKER role hits @PreAuthorize("hasRole('RECRUITER')") → 403
    // This comes from Spring Security BEFORE the controller method runs
    @WithMockUser(roles = "JOB_SEEKER")
    void postJob_shouldReturn403_whenJobSeekerPosts() throws Exception {
        mockMvc.perform(
                post("/jobs")
                        .param("recruiterId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJobRequest)))
                .andExpect(status().isForbidden());

        // @PreAuthorize blocks before the method — service is never called
        verify(jobService, never()).postJob(any(), anyInt());
    }

    @Test
    @DisplayName("POST /jobs → 200 OK when RECRUITER posts valid job")
    @WithMockUser(roles = "RECRUITER")
    void postJob_shouldReturn200_whenRecruiterPosts() throws Exception {
        when(jobService.postJob(any(), anyInt())).thenReturn(testJobResponse);

        mockMvc.perform(
                post("/jobs")
                        .param("recruiterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJobRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.location").value("Bangalore"))
                .andExpect(jsonPath("$.salary").value(80000));
    }

    @Test
    @DisplayName("POST /jobs → 400 when title is blank (validation failure)")
    @WithMockUser(roles = "RECRUITER")
    void postJob_shouldReturn400_whenTitleBlank() throws Exception {
        testJobRequest.setTitle(""); // violates @NotBlank

        mockMvc.perform(
                post("/jobs")
                        .param("recruiterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJobRequest)))
                .andExpect(status().isBadRequest());

        verify(jobService, never()).postJob(any(), anyInt());
    }

    @Test
    @DisplayName("POST /jobs → 409 when duplicate job posted")
    @WithMockUser(roles = "RECRUITER")
    void postJob_shouldReturn409_whenDuplicate() throws Exception {
        when(jobService.postJob(any(), anyInt()))
                .thenThrow(new DuplicateResourceException("Job already posted"));

        mockMvc.perform(
                post("/jobs")
                        .param("recruiterId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJobRequest)))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════════════
    // GET /jobs — PUBLIC (no auth required)
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /jobs → 200 OK without any token (public endpoint)")
    void getAllJobs_shouldReturn200_withoutToken() throws Exception {
        // This endpoint is PUBLIC in your SecurityConfig
        // No @WithMockUser needed — unauthenticated request should succeed
        when(jobService.getAllJobs()).thenReturn(List.of(testJobResponse));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Backend Developer"))
                .andExpect(jsonPath("$[0].location").value("Bangalore"));
    }

    // ═══════════════════════════════════════════════════
    // GET /jobs/search — PUBLIC
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /jobs/search → 200 with filters applied")
    void searchJobs_shouldReturn200_withFilters() throws Exception {
        when(jobService.searchJobs("Backend", "Bangalore", 50000))
                .thenReturn(List.of(testJobResponse));

        mockMvc.perform(get("/jobs/search")
                .param("title", "Backend")
                .param("location", "Bangalore")
                .param("minSalary", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Backend Developer"));
    }

    @Test
    @DisplayName("GET /jobs/search → 200 with no filters (returns all)")
    void searchJobs_shouldReturn200_withNoFilters() throws Exception {
        when(jobService.searchJobs(null, null, null))
                .thenReturn(List.of(testJobResponse));

        // all params are optional — no params = search everything
        mockMvc.perform(get("/jobs/search"))
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════════════
    // GET /jobs/{id} — PUBLIC
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /jobs/{id} → 200 OK when job exists")
    void getJobById_shouldReturn200_whenJobExists() throws Exception {
        when(jobService.getJobById(1)).thenReturn(testJobResponse);

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Developer"));
    }

    @Test
    @DisplayName("GET /jobs/{id} → 404 when job not found")
    void getJobById_shouldReturn404_whenNotFound() throws Exception {
        when(jobService.getJobById(99))
                .thenThrow(new ResourceNotFoundException("Job not found with id: 99"));

        mockMvc.perform(get("/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job not found with id: 99"));
    }

    // ═══════════════════════════════════════════════════
    // GET /jobs/recruiter/{id} — PUBLIC
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("GET /jobs/recruiter/{id} → 200 OK when recruiter exists")
    @WithMockUser(roles = "RECRUITER") // ← add this
    void getJobsByRecruiter_shouldReturn200_whenRecruiterExists() throws Exception {
        when(jobService.getJobsByRecruiter(1)).thenReturn(List.of(testJobResponse));

        mockMvc.perform(get("/jobs/recruiter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Backend Developer"));
    }

    @Test
    @DisplayName("GET /jobs/recruiter/{id} → 404 when recruiter not found")
    @WithMockUser(roles = "JOB_SEEKER") // ← add this (any role works, endpoint isn't role-restricted)
    void getJobsByRecruiter_shouldReturn404_whenNotFound() throws Exception {
        when(jobService.getJobsByRecruiter(99))
                .thenThrow(new ResourceNotFoundException("Recruiter not found with id: 99"));

        mockMvc.perform(get("/jobs/recruiter/99"))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════════════
    // DELETE /jobs/{id} — RECRUITER ONLY
    // ═══════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /jobs/{id} → 401 when no token")
    void deleteJob_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /jobs/{id} → 403 when JOB_SEEKER tries to delete")
    @WithMockUser(roles = "JOB_SEEKER")
    void deleteJob_shouldReturn403_whenJobSeekerDeletes() throws Exception {
        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isForbidden());

        verify(jobService, never()).deleteJob(anyInt());
    }

    @Test
    @DisplayName("DELETE /jobs/{id} → 200 OK when RECRUITER deletes")
    @WithMockUser(roles = "RECRUITER")
    void deleteJob_shouldReturn200_whenRecruiterDeletes() throws Exception {
        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job deleted successfully"));

        verify(jobService, times(1)).deleteJob(1);
    }
}