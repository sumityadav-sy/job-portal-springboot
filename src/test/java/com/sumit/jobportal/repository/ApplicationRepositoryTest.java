package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// ↑ CRITICAL: tells @DataJpaTest "don't replace my datasource with your own"
// without this, @DataJpaTest ignores your H2 URL and uses its default embedded DB
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private User jobSeeker;
    private User recruiter;
    private Job testJob;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        recruiter = new User();
        recruiter.setName("Anita HR");
        recruiter.setEmail("anita@company.com");
        recruiter.setPassword("pass");
        recruiter.setRole(Role.RECRUITER);
        userRepository.save(recruiter);

        jobSeeker = new User();
        jobSeeker.setName("Sumit");
        jobSeeker.setEmail("sumit@gmail.com");
        jobSeeker.setPassword("pass");
        jobSeeker.setRole(Role.JOB_SEEKER);
        userRepository.save(jobSeeker);

        testJob = new Job();
        testJob.setTitle("Backend Developer");
        testJob.setCompany("TechCorp");
        testJob.setLocation("Bangalore");
        testJob.setSalary(80000);
        testJob.setDescription("Spring Boot role");
        testJob.setRecruiter(recruiter);
        jobRepository.save(testJob);

        testApplication = new Application();
        testApplication.setApplicant(jobSeeker);
        testApplication.setJob(testJob);
        testApplication.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(testApplication);
    }

    // ─── TEST 1: findByApplicant ──────────────────────────────────────────────

    @Test
    @DisplayName("findByApplicant → should return applications for that user")
    void findByApplicant_shouldReturnApplications() {

        List<Application> results = applicationRepository.findByApplicant(jobSeeker);

        assertEquals(1, results.size());
        assertEquals(ApplicationStatus.APPLIED, results.get(0).getStatus());
        assertEquals("Sumit", results.get(0).getApplicant().getName());
    }

    // ─── TEST 2: findByApplicant — different user gets empty list ─────────────

    @Test
    @DisplayName("findByApplicant → should return empty list for user with no applications")
    void findByApplicant_shouldReturnEmpty_whenUserHasNoApplications() {

        // recruiter never applied for anything
        List<Application> results = applicationRepository.findByApplicant(recruiter);

        assertTrue(results.isEmpty());
    }

    // ─── TEST 3: findByJob ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByJob → should return applications for that job")
    void findByJob_shouldReturnApplications() {

        List<Application> results = applicationRepository.findByJob(testJob);

        assertEquals(1, results.size());
        assertEquals("Backend Developer", results.get(0).getJob().getTitle());
    }

    // ─── TEST 4: existsByApplicantAndJob — true ───────────────────────────────

    @Test
    @DisplayName("existsByApplicantAndJob → true when application exists")
    void existsByApplicantAndJob_shouldReturnTrue_whenApplicationExists() {

        // jobSeeker already applied to testJob in setUp()
        boolean exists = applicationRepository
                .existsByApplicantAndJob(jobSeeker, testJob);

        assertTrue(exists);
    }

    // ─── TEST 5: existsByApplicantAndJob — false ──────────────────────────────

    @Test
    @DisplayName("existsByApplicantAndJob → false when no application exists")
    void existsByApplicantAndJob_shouldReturnFalse_whenNoApplication() {

        // recruiter never applied to testJob
        boolean exists = applicationRepository
                .existsByApplicantAndJob(recruiter, testJob);

        assertFalse(exists);
    }

    // ─── TEST 6: findByJob — multiple applicants ─────────────────────────────

    @Test
    @DisplayName("findByJob → should return all applicants for a job")
    void findByJob_shouldReturnAllApplicants_whenMultipleApplied() {

        // Add a second applicant
        User secondSeeker = new User();
        secondSeeker.setName("Ravi");
        secondSeeker.setEmail("ravi@gmail.com");
        secondSeeker.setPassword("pass");
        secondSeeker.setRole(Role.JOB_SEEKER);
        userRepository.save(secondSeeker);

        Application secondApp = new Application();
        secondApp.setApplicant(secondSeeker);
        secondApp.setJob(testJob);
        secondApp.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(secondApp);

        List<Application> results = applicationRepository.findByJob(testJob);

        // Both Sumit and Ravi applied to the same job
        assertEquals(2, results.size());
    }
}