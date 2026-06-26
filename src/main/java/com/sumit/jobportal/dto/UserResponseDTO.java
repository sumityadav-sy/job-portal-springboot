package com.sumit.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User data returned in API responses — never includes password")
public class UserResponseDTO {

    @Schema(description = "Auto-generated unique user ID", example = "1")
    private int userId;

    @Schema(description = "Full name of the user", example = "Sumit Kumar")
    private String name;

    @Schema(description = "Email address of the user", example = "sumit@gmail.com")
    private String email;

    @Schema(description = "Role of the user — either JOB_SEEKER or RECRUITER", example = "JOB_SEEKER")
    private String role;

    public UserResponseDTO() {}

    public UserResponseDTO(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}