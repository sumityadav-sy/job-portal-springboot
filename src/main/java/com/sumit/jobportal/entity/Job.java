package com.sumit.jobportal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int jobId;

    private String title;
    private String company;
    private String location;
    private int salary;

    @Column(name = "description", length = 100)
    private String description;

    @CreationTimestamp
    private LocalDateTime postedAt;

    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private User recruiter;

    @JsonIgnore
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<Application> receivedApplications;

    public Job() {}

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public User getRecruiter() { return recruiter; }
    public void setRecruiter(User recruiter) { this.recruiter = recruiter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Application> getReceivedApplications() { return receivedApplications; }
    public void setReceivedApplications(List<Application> receivedApplications) {
        this.receivedApplications = receivedApplications;
    }
}