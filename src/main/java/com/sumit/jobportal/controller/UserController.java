package com.sumit.jobportal.controller;

import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> registerUser(

            // @RequestBody = take the JSON from request body → convert to User object
            // Postman sends: { "name":"Sumit", "email":"sumit@gmail.com", ... }
            // Spring converts that JSON → User java object automatically
            @RequestBody User user) {

        try {
            // send user to service layer → service calls repository → saves to DB
            // returns the saved user (now has userId assigned by MySQL)
            UserResponseDTO saved = userService.registerUser(user);

            // ResponseEntity.status(HttpStatus.CREATED) = HTTP 201
            // 201 = "resource was successfully created" (more specific than 200)
            // .body(saved) = put the saved User object in response (auto-converts to JSON)
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (RuntimeException e) {
            // if service throws exception (e.g. duplicate email)
            // return HTTP 400 Bad Request + the error message as response body
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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

    public ResponseEntity<?> getUserById(

            // @PathVariable = extract {id} from the URL and put it into int id
            // URL: /api/users/5  →  int id = 5  automatically
            @PathVariable int id) {

        try {
            // service finds user by id → if not found, throws RuntimeException
            return ResponseEntity.ok(userService.getUserById(id));

        } catch (RuntimeException e) {
            // HTTP 404 Not Found + "User not found with id: 5"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // ─────────────────────────────────────────────────────
    // ENDPOINT 4 — GET USERS BY ROLE
    // ─────────────────────────────────────────────────────
    // handles GET /api/users/role/JOB_SEEKER
    // or      GET /api/users/role/RECRUITER
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
    // @DeleteMapping("/{id}") = handles DELETE /api/users/5
    // DELETE = "I want to DELETE something"
    @DeleteMapping("/{id}")

    // ResponseEntity<String> = response body will be a String message
    public ResponseEntity<String> deleteUser(@PathVariable int id) {

        try {
            // service deletes user → returns "User deleted successfully"
            return ResponseEntity.ok(userService.deleteUser(id));

        } catch (RuntimeException e) {
            // if user not found → 404 + error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}