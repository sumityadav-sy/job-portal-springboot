package com.sumit.jobportal.service;

import com.sumit.jobportal.entity.*;
import com.sumit.jobportal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    // ─── APPLY FOR A JOB ───────────────────────────────────────────────

    public Application applyForJob(int userId, int jobId) {

        // 1. Check user exists
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. Only JOB_SEEKER can apply — same check you had, now with enum
        if (applicant.getRole() != Role.JOB_SEEKER) {
            throw new RuntimeException("Only job seekers can apply for jobs. User " + userId + " is a " + applicant.getRole());
        }

        // 3. Check job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // 4. Prevent duplicate application — your existsByJobAndUser, now as a derived query
        if (applicationRepository.existsByApplicantAndJob(applicant, job)) {
            throw new RuntimeException("You have already applied for this job.");
        }

        // 5. Build and save application
        Application application = new Application();
        application.setApplicant(applicant);   // full User object, not just ID
        application.setJob(job);               // full Job object, not just ID
        application.setStatus(ApplicationStatus.APPLIED);  // enum, not string

        return applicationRepository.save(application);
    }

    // ─── UPDATE APPLICATION STATUS (RECRUITER ONLY) ────────────────────

    public Application updateStatus(int applicationId, int recruiterId, ApplicationStatus newStatus) {

        // 1. Check application exists
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        // 2. Check recruiter exists
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + recruiterId));

        // 3. Must be a RECRUITER
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new RuntimeException("Only recruiters can update application status.");
        }

        // 4. Recruiter must own the job this application belongs to
        // application.getJob().getRecruiter() gives us the job's recruiter
        if (application.getJob().getRecruiter().getUserId() != recruiterId) {
            throw new RuntimeException("You can only update applications for your own job postings.");
        }

        // 5. Validate status transition — your logic, now with enums
        if (!isValidTransition(application.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition from "
                    + application.getStatus() + " to " + newStatus);
        }

        // 6. Update and save
        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }

    // ─── GET ALL APPLICATIONS BY A USER ────────────────────────────────

    public List<Application> getApplicationsByUser(int userId) {
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return applicationRepository.findByApplicant(applicant);
    }

    // ─── GET ALL APPLICATIONS FOR A JOB ────────────────────────────────

    public List<Application> getApplicationsByJob(int jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        return applicationRepository.findByJob(job);
    }

    // ─── WITHDRAW APPLICATION (JOB SEEKER ONLY) ────────────────────────

    public void withdrawApplication(int applicationId, int userId) {

        // 1. Check application exists
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        // 2. Only the applicant who applied can withdraw their own application
        if (application.getApplicant().getUserId() != userId) {
            throw new RuntimeException("You can only withdraw your own applications.");
        }

        // 3. Can only withdraw if still APPLIED — no point withdrawing an ACCEPTED offer
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("You can only withdraw an application that is still in APPLIED status. Current status: " + application.getStatus());
        }

        applicationRepository.delete(application);
    }

    // ─── STATUS TRANSITION VALIDATOR ───────────────────────────────────
    // Your exact logic from the console project, rewritten with enums

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        switch (current) {
            case APPLIED:
                // Recruiter reviewed it → can move to REVIEWED or directly REJECTED
                return next == ApplicationStatus.REVIEWED || next == ApplicationStatus.REJECTED;
            case REVIEWED:
                // Recruiter made final decision → ACCEPTED or REJECTED
                return next == ApplicationStatus.ACCEPTED || next == ApplicationStatus.REJECTED;
            case ACCEPTED:
                // Final state — no further transitions
                return false;
            case REJECTED:
                // Final state — no further transitions
                return false;
            default:
                return false;
        }
    }
}