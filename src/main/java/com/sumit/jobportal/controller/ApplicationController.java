
package com.sumit.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sumit.jobportal.dto.ApplicationResponseDTO;
import com.sumit.jobportal.entity.ApplicationStatus;
import com.sumit.jobportal.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@Tag(name = "Application Management", description = "Endpoints for applying to jobs, updating status, and withdrawing applications")

public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // JOB_SEEKER ONLY — only job seekers apply for jobs
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // POST /applications?userId=1&jobId=2
    @PostMapping
    @Operation(summary = "Apply for a job", description = "Submits a job application. Only JOB_SEEKER users can apply. "
            +
            "Duplicate applications (same user + same job) are rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "403", description = "User is not a JOB_SEEKER"),
            @ApiResponse(responseCode = "404", description = "User or job not found"),
            @ApiResponse(responseCode = "409", description = "Already applied for this job")
    })
    public ResponseEntity<ApplicationResponseDTO> applyForJob(
            @Parameter(description = "ID of the job seeker applying", example = "1") @RequestParam int userId,

            @Parameter(description = "ID of the job being applied to", example = "3") @RequestParam int jobId) {
        ApplicationResponseDTO application = applicationService.applyForJob(userId, jobId);
        return ResponseEntity.ok(application);
    }

    // RECRUITER ONLY — only recruiters update application status
    @PreAuthorize("hasRole('RECRUITER')")
    // PUT /applications/3/status?recruiterId=1&status=REVIEWED
    @PutMapping("/{id}/status")

    @Operation(summary = "Update application status", description = "Allows a RECRUITER to move an application through the status pipeline. "
            +
            "Valid transitions: APPLIED → REVIEWED or REJECTED, REVIEWED → ACCEPTED or REJECTED. " +
            "ACCEPTED and REJECTED are final states.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "403", description = "User is not a RECRUITER or does not own this job"),
            @ApiResponse(responseCode = "404", description = "Application or recruiter not found")
    })
    public ResponseEntity<ApplicationResponseDTO> updateStatus(
            @Parameter(description = "ID of the application to update", example = "1") @PathVariable int id,

            @Parameter(description = "ID of the recruiter performing the update", example = "2") @RequestParam int recruiterId,

            @Parameter(description = "New status. Valid values: REVIEWED, ACCEPTED, REJECTED", example = "REVIEWED") @RequestParam ApplicationStatus status) {
        ApplicationResponseDTO updated = applicationService.updateStatus(id, recruiterId, status);
        return ResponseEntity.ok(updated);
    }

    // JOB_SEEKER ONLY — seekers view their own applications
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // GET /applications/user/1
    @GetMapping("/user/{userId}")

    @Operation(summary = "Get applications by user", description = "Returns all applications submitted by a specific job seeker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applications returned successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByUser(
            @Parameter(description = "ID of the job seeker", example = "1") @PathVariable int userId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUser(userId));
    }

    // RECRUITER ONLY — recruiters view applications for their jobs
    @PreAuthorize("hasRole('RECRUITER')")
    // GET /applications/job/2
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get applications for a job", description = "Returns all applications received for a specific job listing.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applications returned successfully"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJob(
            @Parameter(description = "ID of the job", example = "3") @PathVariable int jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    // JOB_SEEKER ONLY — only the applicant can withdraw their own application
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // DELETE /applications/3?userId=1
    @DeleteMapping("/{id}")
    @Operation(summary = "Withdraw an application", description = "Allows a job seeker to withdraw their own application. "
            +
            "Only possible while status is still APPLIED — cannot withdraw REVIEWED, ACCEPTED, or REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Application is no longer in APPLIED status"),
            @ApiResponse(responseCode = "401", description = "No JWT token provided"),
            @ApiResponse(responseCode = "403", description = "This is not your application"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<String> withdrawApplication(
            @Parameter(description = "ID of the application to withdraw", example = "1") @PathVariable int id,

            @Parameter(description = "ID of the job seeker withdrawing the application", example = "1") @RequestParam int userId) {
        applicationService.withdrawApplication(id, userId);
        return ResponseEntity.ok("Application withdrawn successfully.");
    }
}