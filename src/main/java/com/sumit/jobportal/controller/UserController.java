package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.UserRequestDTO;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController = this class handles HTTP requests + returns JSON automatically
// without @RestController, Spring wouldn't know this class is an API handler
@RestController

// @RequestMapping = base URL for ALL methods in this class
// every endpoint here will start with /api/users
// example: /api/users, /api/users/1, /api/users/register
@RequestMapping("/api/users")

public class UserController {

    // @Autowired = Spring automatically creates UserService and injects it here
    // you never write: UserService userService = new UserService();
    // Spring manages the object — you just use it
    @Autowired
    private UserService userService;

    // ─────────────────────────────────────────────────────
    // ENDPOINT 1 — CREATE USER
    // ─────────────────────────────────────────────────────
    // @PostMapping("/register") = handles POST requests at /api/users/register
    // POST = "I want to CREATE something"
    @PostMapping("/register")

    // ResponseEntity<?> = lets you control BOTH response body AND status code
    // <?> means "any type" — sometimes we return User, sometimes a String error
    public ResponseEntity<UserResponseDTO> registerUser(

            // @RequestBody = take the JSON from request body → convert to User object
            // Postman sends: { "name":"Sumit", "email":"sumit@gmail.com", ... }
            // Spring converts that JSON → User java object automatically
            @Valid @RequestBody UserRequestDTO user) {

        UserResponseDTO saved = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─────────────────────────────────────────────────────
    // ENDPOINT 2 — GET ALL USERS
    // ─────────────────────────────────────────────────────
    // @GetMapping (no path) = handles GET /api/users
    // GET = "I want to READ something"
    @GetMapping

    // ResponseEntity<List<User>> = response will contain a List of Users
    // no <?> needed here because we always return List<User>, never an error string
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        // userService.getAllUsers() → repository.findAll() → SELECT * FROM users
        // ResponseEntity.ok() = HTTP 200 OK + the list as JSON array
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ─────────────────────────────────────────────────────
    // ENDPOINT 3 — GET USER BY ID
    // ─────────────────────────────────────────────────────
    // @GetMapping("/{id}") = handles GET /api/users/5
    // {id} is a path variable — a placeholder in the URL
    @GetMapping("/{id}")

    public ResponseEntity<UserResponseDTO> getUserById(

            // @PathVariable = extract {id} from the URL and put it into int id
            // URL: /api/users/5 → int id = 5 automatically
            @PathVariable int id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ─────────────────────────────────────────────────────
    // ENDPOINT 4 — GET USERS BY ROLE
    // ─────────────────────────────────────────────────────
    // handles GET /api/users/role/JOB_SEEKER
    // or GET /api/users/role/RECRUITER
    @GetMapping("/role/{role}")

    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(

            // @PathVariable extracts "JOB_SEEKER" from URL
            // Spring automatically converts String "JOB_SEEKER" → Role.JOB_SEEKER enum
            @PathVariable Role role) {

        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // ─────────────────────────────────────────────────────
    // ENDPOINT 5 — DELETE USER
    // ─────────────────────────────────────────────────────

  // Any authenticated user can delete — but service enforces "own account only"
@DeleteMapping("/{id}")
public ResponseEntity<String> deleteUser(
        @PathVariable int id,
        @AuthenticationPrincipal UserDetails currentUser) {
    return ResponseEntity.ok(userService.deleteUser(id, currentUser.getUsername()));
}
}