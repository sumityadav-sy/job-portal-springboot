package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.UserRequestDTO;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

// @Tag names this entire controller in Swagger UI
// instead of "user-controller" (auto-generated from class name)
// it will show as "User Management" with a description underneath
@Tag(name = "User Management", description = "Endpoints for registering users and retrieving user data")

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

    // @Operation describes WHAT this endpoint does — shows as the endpoint title in
    // UI
    // summary = short one-liner shown in the collapsed view
    // description = longer explanation shown when expanded
    @Operation(summary = "Register a new user", description = "Creates a new user account with either JOB_SEEKER or RECRUITER role. " +
"Email must be unique. Password is BCrypt hashed before storage.")

    // @ApiResponses documents every possible HTTP response this endpoint can return
    // Swagger UI shows these in the "Responses" section when you expand an endpoint
    // responseCode = the HTTP status code as a String
    // description = what it means in the context of THIS endpoint
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })

    // @SecurityRequirement(name = "") — this overrides the global security
    // requirement
    // we set in OpenApiConfig (which locks every endpoint by default)
    // empty name = no security required = no lock icon = public endpoint
    // Without this, Swagger UI shows a lock on /register which misleads developers
    @SecurityRequirements()

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

    @Operation(summary = "Get all users", description = "Returns a list of all registered users. Requires RECRUITER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users returned successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "403", description = "Valid token but insufficient role")
    })

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
    @Operation(summary = "Get user by ID", description = "Returns a single user by their numeric ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found and returned"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "No user found with the given ID")
    })

    public ResponseEntity<UserResponseDTO> getUserById(

            // @Parameter documents a @PathVariable or @RequestParam in Swagger UI
            // description = what this value represents
            // example = a realistic sample value shown in the UI input field
            @Parameter(description = "Numeric ID of the user to retrieve", example = "1")

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

    @Operation(summary = "Get users by role", description = "Returns all users that have the specified role. " +
            "Valid values: JOB_SEEKER, RECRUITER")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role value provided"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided")
    })

    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(

            @Parameter(description = "Role to filter by. Must be JOB_SEEKER or RECRUITER", example = "RECRUITER")

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

    @Operation(summary = "Delete a user", description = "Permanently deletes a user account by ID. This also cascades to "
            +
            "their posted jobs (if RECRUITER) and applications (if JOB_SEEKER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "No user found with the given ID")
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "Numeric ID of the user to delete", example = "1") @PathVariable int id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(userService.deleteUser(id, currentUser.getUsername()));
    }
}