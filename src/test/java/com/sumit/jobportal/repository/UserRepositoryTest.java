package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest loads ONLY the JPA slice:
// - Your @Entity classes
// - Your @Repository interfaces
// - EntityManager, transaction management
// Nothing else — no controllers, no services, no security
@DataJpaTest

@ActiveProfiles("test")   // ← tells Spring: activate the "test" profile
                          // this makes it load application-test.properties

// @TestPropertySource loads our H2 config instead of application.properties
@TestPropertySource(locations = "classpath:application-test.properties")

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// ↑ CRITICAL: tells @DataJpaTest "don't replace my datasource with your own"
// without this, @DataJpaTest ignores your H2 URL and uses its default embedded DB
class UserRepositoryTest {

    // @Autowired works here because @DataJpaTest creates real Spring beans
    // for repositories — unlike unit tests where we used @Mock
    // This is a REAL UserRepository backed by a REAL H2 database
    @Autowired
    private UserRepository userRepository;

    private User jobSeeker;
    private User recruiter;

    @BeforeEach
    void setUp() {
        // Clean slate before each test
        // @DataJpaTest wraps each test in a transaction that rolls back,
        // but explicit cleanup makes intent clear
        userRepository.deleteAll();

        jobSeeker = new User();
        jobSeeker.setName("Sumit");
        jobSeeker.setEmail("sumit@gmail.com");
        jobSeeker.setPassword("password123");
        jobSeeker.setRole(Role.JOB_SEEKER);

        recruiter = new User();
        recruiter.setName("Anita HR");
        recruiter.setEmail("anita@company.com");
        recruiter.setPassword("password123");
        recruiter.setRole(Role.RECRUITER);

        // Save both to H2 — these are REAL inserts into a real (in-memory) DB
        userRepository.save(jobSeeker);
        userRepository.save(recruiter);
    }

    // ─── TEST 1: findByEmail — user exists ───────────────────────────────────

    @Test
    @DisplayName("findByEmail → should return user when email exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // ACT — real SELECT query against H2
        Optional<User> result = userRepository.findByEmail("sumit@gmail.com");

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Sumit", result.get().getName());
        assertEquals(Role.JOB_SEEKER, result.get().getRole());
    }

    // ─── TEST 2: findByEmail — user does not exist ────────────────────────────

    @Test
    @DisplayName("findByEmail → should return empty when email not found")
    void findByEmail_shouldReturnEmpty_whenEmailNotFound() {

        Optional<User> result = userRepository.findByEmail("nobody@gmail.com");

        assertFalse(result.isPresent());
    }

    // ─── TEST 3: existsByEmail — true when exists ─────────────────────────────

    @Test
    @DisplayName("existsByEmail → should return true when email is registered")
    void existsByEmail_shouldReturnTrue_whenEmailExists() {

        boolean exists = userRepository.existsByEmail("anita@company.com");

        assertTrue(exists);
    }

    // ─── TEST 4: existsByEmail — false when not exists ───────────────────────

    @Test
    @DisplayName("existsByEmail → should return false when email not registered")
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {

        boolean exists = userRepository.existsByEmail("ghost@gmail.com");

        assertFalse(exists);
    }

    // ─── TEST 5: findByRole ───────────────────────────────────────────────────

    @Test
    @DisplayName("findByRole → should return only users with matching role")
    void findByRole_shouldReturnFilteredUsers() {

        List<User> seekers = userRepository.findByRole(Role.JOB_SEEKER);
        List<User> recruiters = userRepository.findByRole(Role.RECRUITER);

        // Only one of each was saved
        assertEquals(1, seekers.size());
        assertEquals("Sumit", seekers.get(0).getName());

        assertEquals(1, recruiters.size());
        assertEquals("Anita HR", recruiters.get(0).getName());
    }
}