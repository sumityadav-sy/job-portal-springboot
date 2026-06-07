package com.sumit.jobportal.service;

import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.repository.JobRepository;
import com.sumit.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sumit.jobportal.entity.Role;

import java.util.List;
//import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    // Post a new job
    public Job postJob(Job job, int recruiterId) {
    User recruiter = userRepository.findById(recruiterId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + recruiterId));

    // Role check — only RECRUITER can post jobs
    if (recruiter.getRole() != Role.RECRUITER) {
        throw new RuntimeException("Only recruiters can post jobs. User " + recruiterId + " is a " + recruiter.getRole());
    }

    job.setRecruiter(recruiter);
    return jobRepository.save(job);
}
    // Get all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get job by id
    public Job getJobById(int id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    // Get all jobs posted by a specific recruiter
    public List<Job> getJobsByRecruiter(int recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found with id: " + recruiterId));
        return recruiter.getPostedJobs();   // ← this is where List<Job> becomes useful
    }

    // Delete a job
    public void deleteJob(int id) {
        jobRepository.deleteById(id);
    }
}