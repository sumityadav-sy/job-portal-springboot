package com.sumit.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Job listing data returned in API responses")
public class JobResponseDTO {

    @Schema(description = "Auto-generated unique job ID", example = "1")
    private int id;

    @Schema(description = "Job title / position name", example = "Backend Developer")
    private String title;

    @Schema(description = "Brief description of the role", example = "Build REST APIs using Spring Boot")
    private String description;

    @Schema(description = "City or region where the job is based", example = "Bangalore")
    private String location;

    @Schema(description = "Annual salary in INR", example = "800000.0")
    private double salary;

    @Schema(description = "Full name of the recruiter who posted this job", example = "Rahul Sharma")
    private String recruiterName;

    @Schema(description = "Email address of the recruiter", example = "rahul@infosys.com")
    private String recruiterEmail;

    @Schema(description = "Timestamp when the job was posted — auto-set by the server",
            example = "2024-01-15T10:30:00")
    private LocalDateTime postedAt;

    public JobResponseDTO() {}

    public JobResponseDTO(int id, String title, String description,
                          String location, double salary,
                          String recruiterName, String recruiterEmail, LocalDateTime date_time) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.recruiterName = recruiterName;
        this.recruiterEmail = recruiterEmail;
        this.postedAt = date_time;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
}