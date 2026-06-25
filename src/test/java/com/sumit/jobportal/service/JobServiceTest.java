package com.sumit.jobportal.service;

import com.sumit.jobportal.dto.JobRequestDTO;
import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.exception.UnauthorizedActionException;
import com.sumit.jobportal.repository.JobRepository;
import com.sumit.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;

    private User recruiter;
    private User jobSeeker;
    private Job testJob;
    private JobRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // A valid recruiter — can post jobs
        recruiter = new User();
        recruiter.setUserId(1);
        recruiter.setName("Anita HR");
        recruiter.setEmail("anita@company.com");
        recruiter.setPassword("pass123");
        recruiter.setRole(Role.RECRUITER);
        recruiter.setPostedJobs(new ArrayList<>());  // empty list initially

        // A job seeker — cannot post jobs
        jobSeeker = new User();
        jobSeeker.setUserId(2);
        jobSeeker.setName("Sumit");
        jobSeeker.setEmail("sumit@gmail.com");
        jobSeeker.setPassword("pass123");
        jobSeeker.setRole(Role.JOB_SEEKER);

        // A saved Job entity — what the repository returns after save
        testJob = new Job();
        testJob.setJobId(1);
        testJob.setTitle("Backend Developer");
        testJob.setCompany("TechCorp");
        testJob.setLocation("Bangalore");
        testJob.setSalary(80000);
        testJob.setDescription("Spring Boot role");
        testJob.setPostedAt(LocalDateTime.now());
        testJob.setRecruiter(recruiter);  // job knows its recruiter

        // The incoming request DTO — what the controller sends to the service
        testRequest = new JobRequestDTO();
        testRequest.setTitle("Backend Developer");
        testRequest.setCompany("TechCorp");
        testRequest.setLocation("Bangalore");
        testRequest.setSalary(80000);
        testRequest.setDescription("Spring Boot role");
    }

    // ─── TEST 1: Post Job — Happy Path ───────────────────────────────────────

    @Test
    @DisplayName("postJob → should save job and return DTO when recruiter posts")
    void postJob_shouldSaveJob_whenRecruiterPosts() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(recruiter));
        when(jobRepository.existsByTitleAndCompanyAndRecruiter(
                "Backend Developer", "TechCorp", recruiter)).thenReturn(false);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        // ACT
        JobResponseDTO result = jobService.postJob(testRequest, 1);

        // ASSERT
        assertNotNull(result);
        assertEquals("Backend Developer", result.getTitle());
        assertEquals("Anita HR", result.getRecruiterName());  
        // wait — getRecruiterName() returns recruiter's NAME not company
        // let's fix: recruiterName in DTO = recruiter.getName() = "Anita HR"

        verify(jobRepository, times(1)).save(any(Job.class));
    }

    // ─── TEST 2: Post Job — Job Seeker Tries to Post ─────────────────────────

    @Test
    @DisplayName("postJob → should throw UnauthorizedActionException when job seeker posts")
    void postJob_shouldThrowException_whenJobSeekerTriesToPost() {

        // ARRANGE
        // The user found is a JOB_SEEKER, not a RECRUITER
        when(userRepository.findById(2)).thenReturn(Optional.of(jobSeeker));

        // ACT + ASSERT
        assertThrows(UnauthorizedActionException.class,
                () -> jobService.postJob(testRequest, 2));

        // Job should never be saved if role check fails
        verify(jobRepository, never()).save(any(Job.class));
    }

    // ─── TEST 3: Post Job — Recruiter Not Found ───────────────────────────────

    @Test
    @DisplayName("postJob → should throw ResourceNotFoundException when recruiter id is invalid")
    void postJob_shouldThrowException_whenRecruiterNotFound() {

        // ARRANGE
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> jobService.postJob(testRequest, 99));
    }

    // ─── TEST 4: Post Job — Duplicate ────────────────────────────────────────

    @Test
    @DisplayName("postJob → should throw DuplicateResourceException when same job exists")
    void postJob_shouldThrowException_whenDuplicateJob() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(recruiter));

        // existsByTitleAndCompanyAndRecruiter returns true = duplicate exists
        when(jobRepository.existsByTitleAndCompanyAndRecruiter(
                "Backend Developer", "TechCorp", recruiter)).thenReturn(true);

        // ACT + ASSERT
        assertThrows(DuplicateResourceException.class,
                () -> jobService.postJob(testRequest, 1));

        verify(jobRepository, never()).save(any(Job.class));
    }

    // ─── TEST 5: Get All Jobs ─────────────────────────────────────────────────

    @Test
    @DisplayName("getAllJobs → should return list of job DTOs")
    void getAllJobs_shouldReturnListOfDTOs() {

        // ARRANGE
        when(jobRepository.findAll()).thenReturn(List.of(testJob));

        // ACT
        List<JobResponseDTO> result = jobService.getAllJobs();

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Backend Developer", result.get(0).getTitle());
    }

    // ─── TEST 6: Get Job By ID — Happy Path ──────────────────────────────────

    @Test
    @DisplayName("getJobById → should return DTO when job exists")
    void getJobById_shouldReturnDTO_whenJobExists() {

        // ARRANGE
        when(jobRepository.findById(1)).thenReturn(Optional.of(testJob));

        // ACT
        JobResponseDTO result = jobService.getJobById(1);

        // ASSERT
        assertNotNull(result);
        assertEquals("Backend Developer", result.getTitle());
        assertEquals("Bangalore", result.getLocation());
    }

    // ─── TEST 7: Get Job By ID — Not Found ───────────────────────────────────

    @Test
    @DisplayName("getJobById → should throw ResourceNotFoundException when job missing")
    void getJobById_shouldThrowException_whenJobNotFound() {

        // ARRANGE
        when(jobRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> jobService.getJobById(99));
    }

    // ─── TEST 8: Get Jobs By Recruiter ───────────────────────────────────────

    @Test
    @DisplayName("getJobsByRecruiter → should return jobs posted by recruiter")
    void getJobsByRecruiter_shouldReturnJobs_whenRecruiterExists() {

        // ARRANGE
        // The recruiter has one job in their postedJobs list
        recruiter.setPostedJobs(List.of(testJob));
        when(userRepository.findById(1)).thenReturn(Optional.of(recruiter));

        // ACT
        List<JobResponseDTO> result = jobService.getJobsByRecruiter(1);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("Backend Developer", result.get(0).getTitle());
    }

    // ─── TEST 9: Delete Job — Happy Path ─────────────────────────────────────

    @Test
    @DisplayName("deleteJob → should delete when job exists")
    void deleteJob_shouldDelete_whenJobExists() {

        // ARRANGE
        when(jobRepository.existsById(1)).thenReturn(true);
        doNothing().when(jobRepository).deleteById(1);

        // ACT — deleteJob returns void, so we just call it
        assertDoesNotThrow(() -> jobService.deleteJob(1));

        // ASSERT
        verify(jobRepository, times(1)).deleteById(1);
    }

    // ─── TEST 10: Delete Job — Not Found ──────────────────────────────────────

    @Test
    @DisplayName("deleteJob → should throw ResourceNotFoundException when job missing")
    void deleteJob_shouldThrowException_whenJobNotFound() {

        // ARRANGE
        when(jobRepository.existsById(99)).thenReturn(false);

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> jobService.deleteJob(99));

        verify(jobRepository, never()).deleteById(anyInt());
    }
}