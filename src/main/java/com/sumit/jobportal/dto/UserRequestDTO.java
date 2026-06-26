package com.sumit.jobportal.dto;

import com.sumit.jobportal.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// @Schema on the class itself — names and describes this DTO in the Schemas section
@Schema(description = "Request body for registering a new user")
public class UserRequestDTO {

    @Schema(description = "Full name of the user", example = "Sumit Kumar")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Unique email address used for login", example = "sumit@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "Password — minimum 6 characters, stored as BCrypt hash",
            example = "secret123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "Role assigned to this user. Determines what they can do in the system.",
            example = "JOB_SEEKER")
    @NotNull(message = "Role is required")
    private Role role;

    public UserRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}