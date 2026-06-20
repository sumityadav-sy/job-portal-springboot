package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.JobRequestDTO;
import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.service.JobService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // RECRUITER ONLY — only recruiters can post jobs
    // JOB_SEEKER token → 403 before postJob() even runs
    @PreAuthorize("hasRole('RECRUITER')")
    // POST /jobs?recruiterId=1
    @PostMapping
    public ResponseEntity<JobResponseDTO> postJob(@Valid @RequestBody JobRequestDTO job,
            @RequestParam int recruiterId) {
        JobResponseDTO saved = jobService.postJob(job, recruiterId);
        return ResponseEntity.ok(saved);
    }

    // PUBLIC — already open in SecurityConfig, no @PreAuthorize needed
    // GET /jobs
    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    // PUBLIC — open for browsing
    // GET /jobs/search?title=Backend&location=Bangalore&minSalary=80000
    // all params optional — any combination works, including none
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minSalary) {

        return ResponseEntity.ok(jobService.searchJobs(title, location, minSalary));
    }

    // PUBLIC — open for browsing
    // GET /jobs/5
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable int id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // PUBLIC — viewing jobs by recruiter is fine
    // GET /jobs/recruiter/1
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobResponseDTO>> getJobsByRecruiter(@PathVariable int recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

     // RECRUITER ONLY — only recruiters can delete jobs
    @PreAuthorize("hasRole('RECRUITER')")
    // DELETE /jobs/5
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable int id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }
}