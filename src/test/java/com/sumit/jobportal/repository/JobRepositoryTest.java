package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    private User recruiter;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save a recruiter — jobs need a recruiter (FK constraint)
        recruiter = new User();
        recruiter.setName("Anita HR");
        recruiter.setEmail("anita@company.com");
        recruiter.setPassword("pass");
        recruiter.setRole(Role.RECRUITER);
        userRepository.save(recruiter);

        // Job 1: Backend Developer in Bangalore, 80000
        Job job1 = new Job();
        job1.setTitle("Backend Developer");
        job1.setCompany("TechCorp");
        job1.setLocation("Bangalore");
        job1.setSalary(80000);
        job1.setDescription("Spring Boot role");
        job1.setRecruiter(recruiter);

        // Job 2: Frontend Developer in Mumbai, 70000
        Job job2 = new Job();
        job2.setTitle("Frontend Developer");
        job2.setCompany("WebCorp");
        job2.setLocation("Mumbai");
        job2.setSalary(70000);
        job2.setDescription("React role");
        job2.setRecruiter(recruiter);

        // Job 3: Backend Engineer in Bangalore, 60000
        // Note: "Backend" appears in both job1 and job3 title — tests partial match
        Job job3 = new Job();
        job3.setTitle("Backend Engineer");
        job3.setCompany("DataCorp");
        job3.setLocation("Bangalore");
        job3.setSalary(60000);
        job3.setDescription("Java role");
        job3.setRecruiter(recruiter);

        jobRepository.save(job1);
        jobRepository.save(job2);
        jobRepository.save(job3);
    }

    // ─── TEST 1: searchJobs — no filters → returns all ───────────────────────

    @Test
    @DisplayName("searchJobs → should return all jobs when no filters provided")
    void searchJobs_shouldReturnAll_whenNoFilters() {

        // null = "no filter applied" per your JPQL :param IS NULL OR logic
        List<Job> results = jobRepository.searchJobs(null, null, null);

        assertEquals(3, results.size());
    }

    // ─── TEST 2: searchJobs — title filter, partial match ────────────────────

    @Test
    @DisplayName("searchJobs → should return jobs matching partial title")
    void searchJobs_shouldReturnMatches_whenTitleFilterApplied() {

        // "Backend" should match "Backend Developer" AND "Backend Engineer"
        List<Job> results = jobRepository.searchJobs("Backend", null, null);

        assertEquals(2, results.size());
        // Verify both results actually contain "Backend" in title
        assertTrue(results.stream()
                .allMatch(j -> j.getTitle().contains("Backend")));
    }

    // ─── TEST 3: searchJobs — title filter, case-insensitive ─────────────────

    @Test
    @DisplayName("searchJobs → title filter should be case-insensitive")
    void searchJobs_shouldMatchCaseInsensitive_forTitle() {

        // "backend" lowercase should still find "Backend Developer"
        // This tests the LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%')) logic
        List<Job> results = jobRepository.searchJobs("backend", null, null);

        assertEquals(2, results.size());
    }

    // ─── TEST 4: searchJobs — location filter ────────────────────────────────

    @Test
    @DisplayName("searchJobs → should return jobs in matching location")
    void searchJobs_shouldReturnMatches_whenLocationFilterApplied() {

        List<Job> bangaloreJobs = jobRepository.searchJobs(null, "Bangalore", null);
        List<Job> mumbaiJobs = jobRepository.searchJobs(null, "Mumbai", null);

        // job1 and job3 are in Bangalore, job2 is in Mumbai
        assertEquals(2, bangaloreJobs.size());
        assertEquals(1, mumbaiJobs.size());
        assertEquals("Frontend Developer", mumbaiJobs.get(0).getTitle());
    }

    // ─── TEST 5: searchJobs — minSalary filter ───────────────────────────────

    @Test
    @DisplayName("searchJobs → should return jobs at or above minSalary")
    void searchJobs_shouldReturnMatches_whenMinSalaryFilterApplied() {

        // >= 75000 should only return job1 (80000)
        // job2 (70000) and job3 (60000) are below threshold
        List<Job> results = jobRepository.searchJobs(null, null, 75000);

        assertEquals(1, results.size());
        assertEquals("Backend Developer", results.get(0).getTitle());
        assertTrue(results.get(0).getSalary() >= 75000);
    }

    // ─── TEST 6: searchJobs — minSalary boundary (exact match) ───────────────

    @Test
    @DisplayName("searchJobs → should include jobs exactly at minSalary")
    void searchJobs_shouldInclude_whenSalaryExactlyAtMinimum() {

        // >= 70000 should return job1 (80000) and job2 (70000)
        // Tests that the boundary is inclusive
        List<Job> results = jobRepository.searchJobs(null, null, 70000);

        assertEquals(2, results.size());
    }

    // ─── TEST 7: searchJobs — all filters combined ───────────────────────────

    @Test
    @DisplayName("searchJobs → should apply all filters together (AND logic)")
    void searchJobs_shouldApplyAllFilters_whenAllProvided() {

        // "Backend" title + "Bangalore" location + minSalary 70000
        // job1: Backend Developer, Bangalore, 80000 → MATCHES all 3
        // job2: Frontend Developer, Mumbai, 70000 → fails title + location
        // job3: Backend Engineer, Bangalore, 60000 → fails salary
        List<Job> results = jobRepository.searchJobs("Backend", "Bangalore", 70000);

        assertEquals(1, results.size());
        assertEquals("Backend Developer", results.get(0).getTitle());
        assertEquals("Bangalore", results.get(0).getLocation());
    }

    // ─── TEST 8: searchJobs — no matches ─────────────────────────────────────

    @Test
    @DisplayName("searchJobs → should return empty list when nothing matches")
    void searchJobs_shouldReturnEmpty_whenNoMatches() {

        List<Job> results = jobRepository.searchJobs("Python", "Delhi", 500000);

        assertTrue(results.isEmpty());
    }

    // ─── TEST 9: existsByTitleAndCompanyAndRecruiter ──────────────────────────

    @Test
    @DisplayName("existsByTitleAndCompanyAndRecruiter → true when duplicate job exists")
    void existsByTitleAndCompanyAndRecruiter_shouldReturnTrue_whenDuplicate() {

        // "Backend Developer" at "TechCorp" by this recruiter already exists (job1)
        boolean exists = jobRepository.existsByTitleAndCompanyAndRecruiter(
                "Backend Developer", "TechCorp", recruiter);

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByTitleAndCompanyAndRecruiter → false when no duplicate")
    void existsByTitleAndCompanyAndRecruiter_shouldReturnFalse_whenNoDuplicate() {

        // Different title → not a duplicate
        boolean exists = jobRepository.existsByTitleAndCompanyAndRecruiter(
                "DevOps Engineer", "TechCorp", recruiter);

        assertFalse(exists);
    }
}