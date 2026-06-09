package com.sumit.jobportal.dto;

public class ApplicationResponseDTO {

    private int applicationId;
    private String status;

    // Applicant info — flattened from User entity
    private String applicantName;
    private String applicantEmail;

    // Job info — flattened from Job entity
    private int jobId;
    private String jobTitle;
    private String company;

    // Recruiter info — flattened from job's recruiter
    private String recruiterName;

    public ApplicationResponseDTO() {}

    public ApplicationResponseDTO(int applicationId, String status,
                                   String applicantName, String applicantEmail,
                                   int jobId, String jobTitle, String company,
                                   String recruiterName) {
        this.applicationId = applicationId;
        this.status = status;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.recruiterName = recruiterName;
    }

    // Getters and Setters
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
}