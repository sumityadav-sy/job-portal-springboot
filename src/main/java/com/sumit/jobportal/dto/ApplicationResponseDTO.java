package com.sumit.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application data returned in API responses — flattens applicant, job, and recruiter info")
public class ApplicationResponseDTO {

    @Schema(description = "Auto-generated unique application ID", example = "1")
    private int applicationId;

    @Schema(description = "Current status of the application",
            example = "APPLIED",
            allowableValues = {"APPLIED", "REVIEWED", "ACCEPTED", "REJECTED"})
    private String status;

    @Schema(description = "Full name of the job seeker who applied", example = "Amit Verma")
    private String applicantName;

    @Schema(description = "Email address of the job seeker", example = "amit@gmail.com")
    private String applicantEmail;

    @Schema(description = "ID of the job this application belongs to", example = "3")
    private int jobId;

    @Schema(description = "Title of the job applied for", example = "Backend Developer")
    private String jobTitle;

    @Schema(description = "Company that posted the job", example = "Infosys")
    private String company;

    @Schema(description = "Name of the recruiter who owns this job posting", example = "Rahul Sharma")
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