package com.sumit.jobportal.service;

import com.sumit.jobportal.dto.ApplicationResponseDTO;
import com.sumit.jobportal.entity.*;
import com.sumit.jobportal.exception.*;
import com.sumit.jobportal.repository.ApplicationRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    // ── Shared test objects ───────────────────────────────────────────────────

    private User jobSeeker;
    private User recruiter;
    private Job testJob;
    private Application testApplication;

    @BeforeEach
    void setUp() {

        // A valid job seeker
        jobSeeker = new User();
        jobSeeker.setUserId(1);
        jobSeeker.setName("Sumit");
        jobSeeker.setEmail("sumit@gmail.com");
        jobSeeker.setRole(Role.JOB_SEEKER);

        // A valid recruiter who OWNS testJob
        recruiter = new User();
        recruiter.setUserId(2);
        recruiter.setName("Anita HR");
        recruiter.setEmail("anita@company.com");
        recruiter.setRole(Role.RECRUITER);

        // A job posted by recruiter
        testJob = new Job();
        testJob.setJobId(1);
        testJob.setTitle("Backend Developer");
        testJob.setCompany("TechCorp");
        testJob.setLocation("Bangalore");
        testJob.setSalary(80000);
        testJob.setDescription("Spring Boot role");
        testJob.setPostedAt(LocalDateTime.now());
        testJob.setRecruiter(recruiter); // recruiter owns this job

        // An application: jobSeeker applied to testJob, status = APPLIED
        testApplication = new Application();
        testApplication.setApplicationId(1);
        testApplication.setApplicant(jobSeeker);
        testApplication.setJob(testJob);
        testApplication.setStatus(ApplicationStatus.APPLIED);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // applyForJob() TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── TEST 1: Apply — Happy Path ───────────────────────────────────────────

    @Test
    @DisplayName("applyForJob → should create application when valid job seeker applies")
    void applyForJob_shouldCreateApplication_whenValid() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(jobSeeker));
        when(jobRepository.findById(1)).thenReturn(Optional.of(testJob));
        when(applicationRepository.existsByApplicantAndJob(jobSeeker, testJob))
                .thenReturn(false); // not a duplicate
        when(applicationRepository.save(any(Application.class)))
                .thenReturn(testApplication);

        // ACT
        ApplicationResponseDTO result = applicationService.applyForJob(1, 1);

        // ASSERT
        assertNotNull(result);
        assertEquals("Sumit", result.getApplicantName());
        assertEquals("Backend Developer", result.getJobTitle());
        assertEquals("APPLIED", result.getStatus());

        // confirm save was called — application was actually persisted
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    // ─── TEST 2: Apply — User Not Found ──────────────────────────────────────

    @Test
    @DisplayName("applyForJob → should throw ResourceNotFoundException when user missing")
    void applyForJob_shouldThrow_whenUserNotFound() {

        // ARRANGE
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(99, 1));

        // nothing else should be called after user lookup fails
        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 3: Apply — Recruiter Tries to Apply ────────────────────────────

    @Test
    @DisplayName("applyForJob → should throw UnauthorizedActionException when recruiter applies")
    void applyForJob_shouldThrow_whenRecruiterTriesToApply() {

        // ARRANGE
        // recruiter has Role.RECRUITER — only JOB_SEEKER can apply
        when(userRepository.findById(2)).thenReturn(Optional.of(recruiter));

        // ACT + ASSERT
        assertThrows(UnauthorizedActionException.class,
                () -> applicationService.applyForJob(2, 1));

        // this is the key verify: role check must stop execution before job lookup
        // if jobRepository.findById was called, the role check didn't abort properly
        verify(jobRepository, never()).findById(anyInt());
        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 4: Apply — Job Not Found ───────────────────────────────────────

    @Test
    @DisplayName("applyForJob → should throw ResourceNotFoundException when job missing")
    void applyForJob_shouldThrow_whenJobNotFound() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(jobSeeker));
        when(jobRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(1, 99));

        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 5: Apply — Duplicate Application ────────────────────────────────

    @Test
    @DisplayName("applyForJob → should throw DuplicateResourceException when already applied")
    void applyForJob_shouldThrow_whenAlreadyApplied() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(jobSeeker));
        when(jobRepository.findById(1)).thenReturn(Optional.of(testJob));

        // existsByApplicantAndJob returns true = already applied
        when(applicationRepository.existsByApplicantAndJob(jobSeeker, testJob))
                .thenReturn(true);

        // ACT + ASSERT
        assertThrows(DuplicateResourceException.class,
                () -> applicationService.applyForJob(1, 1));

        verify(applicationRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // updateStatus() TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── TEST 6: Update Status — Happy Path (APPLIED → REVIEWED) ─────────────

    @Test
    @DisplayName("updateStatus → should update status when recruiter owns the job")
    void updateStatus_shouldUpdate_whenRecruiterOwnsJob() {

        // ARRANGE
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(2)).thenReturn(Optional.of(recruiter));

        // After save, return an application with updated status
        Application reviewedApplication = new Application();
        reviewedApplication.setApplicationId(1);
        reviewedApplication.setApplicant(jobSeeker);
        reviewedApplication.setJob(testJob);
        reviewedApplication.setStatus(ApplicationStatus.REVIEWED);

        when(applicationRepository.save(any(Application.class)))
                .thenReturn(reviewedApplication);

        // ACT
        ApplicationResponseDTO result = applicationService.updateStatus(
                1, 2, ApplicationStatus.REVIEWED);

        // ASSERT
        assertNotNull(result);
        assertEquals("REVIEWED", result.getStatus());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    // ─── TEST 7: Update Status — Application Not Found ───────────────────────

    @Test
    @DisplayName("updateStatus → should throw ResourceNotFoundException when application missing")
    void updateStatus_shouldThrow_whenApplicationNotFound() {

        // ARRANGE
        when(applicationRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.updateStatus(99, 2, ApplicationStatus.REVIEWED));

        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 8: Update Status — Recruiter Not Found ──────────────────────────

    @Test
    @DisplayName("updateStatus → should throw ResourceNotFoundException when recruiter missing")
    void updateStatus_shouldThrow_whenRecruiterNotFound() {

        // ARRANGE
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.updateStatus(1, 99, ApplicationStatus.REVIEWED));

        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 9: Update Status — Job Seeker Tries to Update ──────────────────

    @Test
    @DisplayName("updateStatus → should throw UnauthorizedActionException when job seeker updates")
    void updateStatus_shouldThrow_whenJobSeekerUpdates() {

        // ARRANGE
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        // jobSeeker has Role.JOB_SEEKER — only RECRUITER can update status
        when(userRepository.findById(1)).thenReturn(Optional.of(jobSeeker));

        // ACT + ASSERT
        assertThrows(UnauthorizedActionException.class,
                () -> applicationService.updateStatus(1, 1, ApplicationStatus.REVIEWED));

        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 10: Update Status — Recruiter Doesn't Own The Job ──────────────

    @Test
    @DisplayName("updateStatus → should throw UnauthorizedActionException when recruiter doesn't own job")
    void updateStatus_shouldThrow_whenRecruiterDoesntOwnJob() {

        // ARRANGE
        // A DIFFERENT recruiter — not the one who posted testJob
        User otherRecruiter = new User();
        otherRecruiter.setUserId(3);
        otherRecruiter.setName("Other HR");
        otherRecruiter.setEmail("other@company.com");
        otherRecruiter.setRole(Role.RECRUITER);
        // Note: testJob.getRecruiter().getUserId() = 2 (recruiter)
        // otherRecruiter.getUserId() = 3 → mismatch → should be blocked

        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(3)).thenReturn(Optional.of(otherRecruiter));

        // ACT + ASSERT
        assertThrows(UnauthorizedActionException.class,
                () -> applicationService.updateStatus(1, 3, ApplicationStatus.REVIEWED));

        verify(applicationRepository, never()).save(any());
    }

    // ─── TEST 11: Update Status — Invalid Transition (APPLIED → ACCEPTED) ────

    @Test
    @DisplayName("updateStatus → should throw InvalidInputException for invalid status transition")
    void updateStatus_shouldThrow_whenInvalidTransition() {

        // ARRANGE
        // testApplication is APPLIED — jumping straight to ACCEPTED is invalid
        // valid path is APPLIED → REVIEWED → ACCEPTED
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(2)).thenReturn(Optional.of(recruiter));

        // ACT + ASSERT
        // APPLIED → ACCEPTED skips REVIEWED — your isValidTransition() blocks this
        assertThrows(InvalidInputException.class,
                () -> applicationService.updateStatus(1, 2, ApplicationStatus.ACCEPTED));

        verify(applicationRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // withdrawApplication() TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── TEST 12: Withdraw — Happy Path ──────────────────────────────────────

    @Test
    @DisplayName("withdrawApplication → should delete when applicant withdraws own APPLIED application")
    void withdrawApplication_shouldDelete_whenValid() {

        // ARRANGE
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        // testApplication.getApplicant().getUserId() = 1 = jobSeeker
        // userId param = 1 = matches → authorized
        doNothing().when(applicationRepository).delete(testApplication);

        // ACT
        assertDoesNotThrow(() -> applicationService.withdrawApplication(1, 1));

        // ASSERT
        // delete(entity) not deleteById — your service passes the full object
        verify(applicationRepository, times(1)).delete(testApplication);
    }

    // ─── TEST 13: Withdraw — Application Not Found ───────────────────────────

    @Test
    @DisplayName("withdrawApplication → should throw ResourceNotFoundException when application missing")
    void withdrawApplication_shouldThrow_whenApplicationNotFound() {

        // ARRANGE
        when(applicationRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.withdrawApplication(99, 1));

        verify(applicationRepository, never()).delete(any());
    }

    // ─── TEST 14: Withdraw — Wrong User Tries to Withdraw ────────────────────

    @Test
    @DisplayName("withdrawApplication → should throw UnauthorizedActionException when different user withdraws")
    void withdrawApplication_shouldThrow_whenWrongUserWithdraws() {

        // ARRANGE
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));
        // testApplication belongs to userId=1 (jobSeeker)
        // userId=99 is a different person trying to withdraw it

        // ACT + ASSERT
        assertThrows(UnauthorizedActionException.class,
                () -> applicationService.withdrawApplication(1, 99));

        verify(applicationRepository, never()).delete(any());
    }

    // ─── TEST 15: Withdraw — Application Already Reviewed ────────────────────

    @Test
    @DisplayName("withdrawApplication → should throw InvalidInputException when status is not APPLIED")
    void withdrawApplication_shouldThrow_whenStatusIsNotApplied() {

        // ARRANGE
        // Change the application status to REVIEWED — can no longer be withdrawn
        testApplication.setStatus(ApplicationStatus.REVIEWED);
        when(applicationRepository.findById(1)).thenReturn(Optional.of(testApplication));

        // ACT + ASSERT
        // Your service rule: can only withdraw if status == APPLIED
        assertThrows(InvalidInputException.class,
                () -> applicationService.withdrawApplication(1, 1));

        verify(applicationRepository, never()).delete(any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getApplicationsByUser() and getApplicationsByJob() TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── TEST 16: Get Applications By User ───────────────────────────────────

    @Test
    @DisplayName("getApplicationsByUser → should return list of DTOs for valid user")
    void getApplicationsByUser_shouldReturnList_whenUserExists() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(jobSeeker));
        when(applicationRepository.findByApplicant(jobSeeker))
                .thenReturn(List.of(testApplication));

        // ACT
        List<ApplicationResponseDTO> result =
                applicationService.getApplicationsByUser(1);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sumit", result.get(0).getApplicantName());
        assertEquals("Backend Developer", result.get(0).getJobTitle());
    }

    // ─── TEST 17: Get Applications By Job ────────────────────────────────────

    @Test
    @DisplayName("getApplicationsByJob → should return list of DTOs for valid job")
    void getApplicationsByJob_shouldReturnList_whenJobExists() {

        // ARRANGE
        when(jobRepository.findById(1)).thenReturn(Optional.of(testJob));
        when(applicationRepository.findByJob(testJob))
                .thenReturn(List.of(testApplication));

        // ACT
        List<ApplicationResponseDTO> result =
                applicationService.getApplicationsByJob(1);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Backend Developer", result.get(0).getJobTitle());
        assertEquals("APPLIED", result.get(0).getStatus());
    }
}