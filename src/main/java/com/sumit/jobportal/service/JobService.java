package com.sumit.jobportal.service;

import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.repository.JobRepository;
import com.sumit.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sumit.jobportal.entity.Role;

import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    // Post a new job
    public JobResponseDTO postJob(Job job, int recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + recruiterId));

        // Role check — only RECRUITER can post jobs
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new RuntimeException(
                    "Only recruiters can post jobs. User " + recruiterId + " is a " + recruiter.getRole());
        }

        job.setRecruiter(recruiter);
        return convertToDTO(jobRepository.save(job));
    }

    // Get all jobs
    public List<JobResponseDTO> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        return jobs.stream()
                .map(job -> convertToDTO(job))
                .toList();
    }

    // Get job by id
    public JobResponseDTO getJobById(int id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return convertToDTO(job);
    }

    // Get all jobs posted by a specific recruiter
    public List<JobResponseDTO> getJobsByRecruiter(int recruiterId) {
        List<Job> jobs =new ArrayList<>();
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found with id: " + recruiterId));
         jobs = recruiter.getPostedJobs(); // ← this is where List<Job> becomes useful
        List<JobResponseDTO> result = new ArrayList<>();
        for (Job job : jobs) {
            result.add(convertToDTO(job));
        }
        return result;
    }

    // Delete a job
    public void deleteJob(int id) {
        jobRepository.deleteById(id);
    }

    // Add this private helper method in JobService
    private JobResponseDTO convertToDTO(Job job) {
        return new JobResponseDTO(
                job.getJobId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getRecruiter().getName(), // pulls just the name from User
                job.getRecruiter().getEmail(), // pulls just the email from User
                job.getPostedAt()
        );
    }
}