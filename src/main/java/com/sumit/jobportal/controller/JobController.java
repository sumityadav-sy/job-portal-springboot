package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.service.JobService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // POST /jobs?recruiterId=1
    @PostMapping
    public ResponseEntity<JobResponseDTO> postJob(@Valid @RequestBody Job job,
            @RequestParam int recruiterId) {
        JobResponseDTO saved = jobService.postJob(job, recruiterId);
        return ResponseEntity.ok(saved);
    }

    // GET /jobs
    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    // GET /jobs/search?title=Backend&location=Bangalore&minSalary=80000
    // all params optional — any combination works, including none
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minSalary) {

        return ResponseEntity.ok(jobService.searchJobs(title, location, minSalary));
    }

    // GET /jobs/5
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable int id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // GET /jobs/recruiter/1
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobResponseDTO>> getJobsByRecruiter(@PathVariable int recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    // DELETE /jobs/5
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable int id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }
}