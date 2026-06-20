
package com.sumit.jobportal.controller;

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
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // JOB_SEEKER ONLY — only job seekers apply for jobs
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // POST /applications?userId=1&jobId=2
    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> applyForJob(@RequestParam int userId,
            @RequestParam int jobId) {
        ApplicationResponseDTO application = applicationService.applyForJob(userId, jobId);
        return ResponseEntity.ok(application);
    }

    // RECRUITER ONLY — only recruiters update application status
    @PreAuthorize("hasRole('RECRUITER')")
    // PUT /applications/3/status?recruiterId=1&status=REVIEWED
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponseDTO> updateStatus(@PathVariable int id,
            @RequestParam int recruiterId,
            @RequestParam ApplicationStatus status) {
        ApplicationResponseDTO updated = applicationService.updateStatus(id, recruiterId, status);
        return ResponseEntity.ok(updated);
    }

    // JOB_SEEKER ONLY — seekers view their own applications
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // GET /applications/user/1
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUser(userId));
    }

    // RECRUITER ONLY — recruiters view applications for their jobs
    @PreAuthorize("hasRole('RECRUITER')")
    // GET /applications/job/2
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJob(@PathVariable int jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    // JOB_SEEKER ONLY — only the applicant can withdraw their own application
    @PreAuthorize("hasRole('JOB_SEEKER')")
    // DELETE /applications/3?userId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<String> withdrawApplication(@PathVariable int id,
            @RequestParam int userId) {
        applicationService.withdrawApplication(id, userId);
        return ResponseEntity.ok("Application withdrawn successfully.");
    }
}