package com.sumit.jobportal.service;

import com.sumit.jobportal.dto.JobRequestDTO;
import com.sumit.jobportal.dto.JobResponseDTO;
import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.exception.UnauthorizedActionException;
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
    public JobResponseDTO postJob(JobRequestDTO request, int recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recruiterId));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new UnauthorizedActionException(
                    "Only recruiters can post jobs. User " + recruiterId + " is a " + recruiter.getRole());
        }

        if (jobRepository.existsByTitleAndCompanyAndRecruiter(
                request.getTitle(), request.getCompany(), recruiter)) {
            throw new DuplicateResourceException(
                    "You have already posted a job for '" + request.getTitle() + "' at " + request.getCompany());
        }

        // map DTO → entity
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setDescription(request.getDescription());
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

    // Search jobs with optional filters
    public List<JobResponseDTO> searchJobs(String title, String location, Integer minSalary) {
        return jobRepository.searchJobs(title, location, minSalary)
                .stream()
                .map(job -> convertToDTO(job))
                .toList();
    }

    // Get job by id
    public JobResponseDTO getJobById(int id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return convertToDTO(job);
    }

    // Get all jobs posted by a specific recruiter
    public List<JobResponseDTO> getJobsByRecruiter(int recruiterId) {
        List<Job> jobs = new ArrayList<>();
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));
        jobs = recruiter.getPostedJobs(); // ← this is where List<Job> becomes useful
        List<JobResponseDTO> result = new ArrayList<>();
        for (Job job : jobs) {
            result.add(convertToDTO(job));
        }
        return result;
    }

    // Delete a job
    public void deleteJob(int id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("job not found with id: " + id);
        }
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
                job.getPostedAt());
    }
}