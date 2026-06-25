package com.sumit.jobportal.service;

import com.sumit.jobportal.dto.UserRequestDTO;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.exception.UnauthorizedActionException;
import com.sumit.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith tells JUnit: "before running any test in this class,
// activate the Mockito extension". The extension is responsible for:
// 1. Scanning the class for @Mock and @InjectMocks annotations
// 2. Creating mock objects for every @Mock field
// 3. Injecting those mocks into the @InjectMocks object
// Without this, @Mock and @InjectMocks are just ignored annotations
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // ← FIX 1: UserService now uses passwordEncoder (added during JWT phase)
    // Without this mock, passwordEncoder is null → NullPointerException
    @Mock
    private PasswordEncoder passwordEncoder;

    // @Mock creates a FAKE UserRepository.
    // It has all the same methods (findById, save, existsByEmail, etc.)
    // but NONE of them do anything by default — they return null/empty/false.
    // You control what they return using when().thenReturn() in each test.
    // Crucially: no database connection is ever made.
    @Mock
    private UserRepository userRepository;

    // @InjectMocks creates a REAL UserService instance.
    // Mockito looks at UserService's fields, sees it needs a UserRepository,
    // and injects the mock UserRepository we defined above.
    // Result: a real UserService with a fake repository — exactly what we want.
    @InjectMocks
    private UserService userService;

    // These are reusable test objects — built once, used across multiple tests.
    // Declared here so every test method can access them.
    private User testUser;
    private UserRequestDTO testRequest;

    // @BeforeEach runs BEFORE EVERY single @Test method.
    // Think of it as "reset the world before each test".
    // This ensures tests don't share state — each test starts fresh.
    @BeforeEach
    void setUp() {
        // Build a fake User entity — the kind our mock repository would return
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("Sumit");
        testUser.setEmail("sumit@gmail.com");
        testUser.setPassword("hashed_password_123"); // what DB would store
        testUser.setRole(Role.JOB_SEEKER);

        // Build a fake request DTO — the kind our controller would receive
        testRequest = new UserRequestDTO();
        testRequest.setName("Sumit");
        testRequest.setEmail("sumit@gmail.com");
        testRequest.setPassword("password123");// raw password from request
        testRequest.setRole(Role.JOB_SEEKER);
    }

    // ─── TEST 1: Register User — Happy Path ──────────────────────────────────

    // @DisplayName gives this test a human-readable label in the test report.
    // Instead of seeing "registerUser_shouldSaveAndReturnDTO" you see this
    // sentence.
    @Test
    @DisplayName("registerUser → should save user and return response DTO")
    void registerUser_shouldSaveAndReturnDTO() {

        // ── ARRANGE ──────────────────────────────────────────────────────────
        // "When the repository checks if this email exists, return false"
        // (false = email is NOT taken = registration should proceed)
        when(userRepository.existsByEmail("sumit@gmail.com")).thenReturn(false);

        // ← FIX 1 continued: stub the encoder so it returns something predictable
        // "when encode() is called with any string, return this fake hash"
        // This prevents the NullPointerException from passwordEncoder.encode()
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password_123");

        // "When the repository saves any User object, return our testUser"
        // any(User.class) means "I don't care which exact User object is passed,
        // just return testUser whenever save() is called with any User"
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // ── ACT ──────────────────────────────────────────────────────────────
        // Call the REAL method we are testing
        UserResponseDTO result = userService.registerUser(testRequest);

        // ── ASSERT ───────────────────────────────────────────────────────────
        // Check the returned DTO has the correct values
        assertNotNull(result); // result was not null
        assertEquals("Sumit", result.getName()); // name mapped correctly
        assertEquals("sumit@gmail.com", result.getEmail()); // email mapped correctly
        assertEquals("JOB_SEEKER", result.getRole()); // enum converted to String

        // verify() checks that the mock was actually called as expected.
        // This confirms UserService did call save() exactly once —
        // not zero times (bug: forgot to save) and not twice (bug: saved twice)
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ─── TEST 2: Register User — Duplicate Email ──────────────────────────────

    @Test
    @DisplayName("registerUser → should throw DuplicateResourceException when email exists")
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {

        // ARRANGE
        // "When the repository checks this email, return true" (email IS taken)
        when(userRepository.existsByEmail("sumit@gmail.com")).thenReturn(true);

        // ACT + ASSERT combined
        // assertThrows verifies that calling registerUser() THROWS the expected
        // exception.
        // If no exception is thrown, this test FAILS.
        // If a different exception is thrown, this test FAILS.
        // Lambda syntax: () -> userService.registerUser(testRequest)
        // means "execute this code and watch for exceptions"
        assertThrows(DuplicateResourceException.class,
                () -> userService.registerUser(testRequest));

        // verify save was NEVER called — if email exists, we should not reach save()
        // This is an important negative assertion: confirm the service aborted early
        verify(userRepository, never()).save(any(User.class));
    }

    // ─── TEST 3: Get User By ID — Happy Path ─────────────────────────────────

    @Test
    @DisplayName("getUserById → should return DTO when user exists")
    void getUserById_shouldReturnDTO_whenUserExists() {

        // ARRANGE
        // findById returns Optional — so we wrap our testUser in Optional.of()
        // Optional.of(testUser) = "yes, user was found, here it is"
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // ACT
        UserResponseDTO result = userService.getUserById(1);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("Sumit", result.getName());
    }

    // ─── TEST 4: Get User By ID — Not Found ──────────────────────────────────

    @Test
    @DisplayName("getUserById → should throw ResourceNotFoundException when user missing")
    void getUserById_shouldThrowException_whenUserNotFound() {

        // ARRANGE
        // Optional.empty() = "no user found with this id"
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99));
    }

    // ─── TEST 5: Get All Users ────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers → should return list of DTOs")
    void getAllUsers_shouldReturnListOfDTOs() {

        // ARRANGE
        // Create a second user for the list
        User secondUser = new User();
        secondUser.setUserId(2);
        secondUser.setName("Ravi");
        secondUser.setEmail("ravi@gmail.com");
        secondUser.setPassword("pass123");
        secondUser.setRole(Role.RECRUITER);

        when(userRepository.findAll()).thenReturn(List.of(testUser, secondUser));

        // ACT
        List<UserResponseDTO> result = userService.getAllUsers();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size()); // exactly 2 users returned
        assertEquals("Sumit", result.get(0).getName());
        assertEquals("Ravi", result.get(1).getName());
    }

    // ─── TEST 6: Get Users By Role ────────────────────────────────────────────

    @Test
    @DisplayName("getUsersByRole → should return only users with that role")
    void getUsersByRole_shouldReturnFilteredList() {

        // ARRANGE
        when(userRepository.findByRole(Role.JOB_SEEKER)).thenReturn(List.of(testUser));

        // ACT
        List<UserResponseDTO> result = userService.getUsersByRole(Role.JOB_SEEKER);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("JOB_SEEKER", result.get(0).getRole());
    }

    // ─── TEST 7: Delete User — Happy Path ────────────────────────────────────

    @Test
    @DisplayName("deleteUser → should delete successfully when user exists")
    void deleteUser_shouldDelete_whenUserExists() {

        // ARRANGE
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("sumit@gmail.com")).thenReturn(Optional.of(testUser));
        
        // ACT
        String result = userService.deleteUser(1, "sumit@gmail.com");

        // ASSERT
        assertEquals("User deleted successfully", result);

        // Confirm deleteById was actually called — not skipped
        verify(userRepository, times(1)).deleteById(1);
    }

    // ─── TEST 7b: Delete User — Recruiter deletes another user ───────────────

@Test
@DisplayName("deleteUser → recruiter should be able to delete any user")
void deleteUser_shouldDelete_whenRecruiterDeletes() {

    // ARRANGE
    // recruiter is deleting testUser (different person)
    User recruiter = new User();
    recruiter.setUserId(2);
    recruiter.setName("HR Manager");
    recruiter.setEmail("hr@company.com");
    recruiter.setPassword("pass");
    recruiter.setRole(Role.RECRUITER);

    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    // requestingEmail is recruiter's email, not testUser's
    when(userRepository.findByEmail("hr@company.com")).thenReturn(Optional.of(recruiter));

    // ACT
    String result = userService.deleteUser(1, "hr@company.com");

    // ASSERT
    assertEquals("User deleted successfully", result);
    verify(userRepository, times(1)).deleteById(1);
}

// ─── TEST 7c: Delete User — Unauthorized (job seeker deleting someone else) ──

@Test
@DisplayName("deleteUser → should throw UnauthorizedActionException when job seeker deletes another user")
void deleteUser_shouldThrowException_whenUnauthorized() {

    // ARRANGE
    User anotherJobSeeker = new User();
    anotherJobSeeker.setUserId(2);
    anotherJobSeeker.setName("Ravi");
    anotherJobSeeker.setEmail("ravi@gmail.com");
    anotherJobSeeker.setPassword("pass");
    anotherJobSeeker.setRole(Role.JOB_SEEKER); // not a recruiter

    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    // requester is a different job seeker — neither self nor recruiter
    when(userRepository.findByEmail("ravi@gmail.com")).thenReturn(Optional.of(anotherJobSeeker));

    // ACT + ASSERT
    assertThrows(UnauthorizedActionException.class,
            () -> userService.deleteUser(1, "ravi@gmail.com"));

    verify(userRepository, never()).deleteById(anyInt());
}
    // ─── TEST 8: Delete User — Not Found ─────────────────────────────────────

    @Test
    @DisplayName("deleteUser → should throw ResourceNotFoundException when user missing")
    void deleteUser_shouldThrowException_whenUserNotFound() {

        // ARRANGE
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(99, "sumit@gmail.com"));

        // Confirm deleteById was NEVER called — service should abort before reaching it
        verify(userRepository, never()).deleteById(anyInt());
    }
}