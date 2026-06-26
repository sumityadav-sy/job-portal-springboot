package com.sumit.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for posting a new job listing")
public class JobRequestDTO {

    @Schema(description = "Job title / position name", example = "Backend Developer")
    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Schema(description = "Name of the hiring company", example = "Infosys")
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String company;

    @Schema(description = "City or region where the job is based", example = "Bangalore")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Annual salary offered in INR — must be zero or positive",
            example = "800000")
    @Min(value = 0, message = "Salary must be a positive number")
    private int salary;

    @Schema(description = "Brief description of the role and responsibilities",
            example = "Build and maintain REST APIs using Spring Boot")
    @NotBlank(message = "Description is required")
    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;

    public JobRequestDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}