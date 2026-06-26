package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.JobRequestDTO;
import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.service.JobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@Tag(name = "Job Management", description = "Endpoints for posting, searching, and managing jobs")

public class JobController {

    @Autowired
    private JobService jobService;

    // RECRUITER ONLY — only recruiters can post jobs
    // JOB_SEEKER token → 403 before postJob() even runs
    @PreAuthorize("hasRole('RECRUITER')")
    // POST /jobs?recruiterId=1
    @PostMapping

    @Operation(summary = "Post a new job", description = "Creates a new job listing. Only users with RECRUITER role can post jobs. "
            +
            "Duplicate job title + company combination for the same recruiter is rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job posted successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "403", description = "User is not a RECRUITER"),
            @ApiResponse(responseCode = "404", description = "Recruiter user not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate job — same title and company already posted by this recruiter")
    })
    public ResponseEntity<JobResponseDTO> postJob(@Valid @RequestBody JobRequestDTO job,
            @Parameter(description = "ID of the recruiter posting the job", example = "2")

            @RequestParam int recruiterId) {
        JobResponseDTO saved = jobService.postJob(job, recruiterId);
        return ResponseEntity.ok(saved);
    }

    // PUBLIC — already open in SecurityConfig, no @PreAuthorize needed
    // GET /jobs
    @GetMapping
    @Operation(summary = "Get all jobs", description = "Returns all job listings. This is a public endpoint — no authentication required.")
    @ApiResponse(responseCode = "200", description = "All jobs returned successfully")

    // public endpoint — override global security lock
    @SecurityRequirements()
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    // PUBLIC — open for browsing
    // GET /jobs/search?title=Backend&location=Bangalore&minSalary=80000
    // all params optional — any combination works, including none
    @GetMapping("/search")
    @Operation(summary = "Search jobs with filters", description = "Search jobs by any combination of title, location, and minimum salary. "
            +
            "All parameters are optional — omitting all returns all jobs. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching jobs returned (empty list if none match)")
    })
    @SecurityRequirements()
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @Parameter(description = "Partial or full job title to search for", example = "Backend") @RequestParam(required = false) String title,

            @Parameter(description = "City or location to filter by", example = "Bangalore") @RequestParam(required = false) String location,

            @Parameter(description = "Minimum salary filter (inclusive)", example = "50000") @RequestParam(required = false) Integer minSalary) {

        return ResponseEntity.ok(jobService.searchJobs(title, location, minSalary));
    }

    // PUBLIC — open for browsing
    // GET /jobs/5
    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Returns a single job by its numeric ID. Public endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job found and returned"),
            @ApiResponse(responseCode = "404", description = "No job found with the given ID")
    })
    @SecurityRequirements()
    public ResponseEntity<JobResponseDTO> getJobById(
            @Parameter(description = "Numeric ID of the job", example = "1") @PathVariable int id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // PUBLIC — viewing jobs by recruiter is fine
    // GET /jobs/recruiter/1
    @GetMapping("/recruiter/{recruiterId}")
    @Operation(summary = "Get all jobs by a recruiter", description = "Returns all jobs posted by a specific recruiter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs returned successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "Recruiter not found")
    })
    public ResponseEntity<List<JobResponseDTO>> getJobsByRecruiter(
            @Parameter(description = "Numeric ID of the recruiter", example = "2") @PathVariable int recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    // RECRUITER ONLY — only recruiters can delete jobs
    @PreAuthorize("hasRole('RECRUITER')")
    // DELETE /jobs/5
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job", description = "Permanently deletes a job listing by ID. " +
            "All applications linked to this job are also deleted (cascade).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "No job found with the given ID")
    })

    public ResponseEntity<String> deleteJob(
            @Parameter(description = "Numeric ID of the job to delete", example = "1")
            @PathVariable int id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }
}