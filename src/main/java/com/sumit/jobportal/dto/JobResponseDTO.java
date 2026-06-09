package com.sumit.jobportal.dto;

import java.time.LocalDateTime;

public class JobResponseDTO {

    private int id;
    private String title;
    private String description;
    private String location;
    private double salary;
    private String recruiterName;  // just the name, not the whole User object
    private String recruiterEmail; // just the email
    private LocalDateTime postedAt;


    public JobResponseDTO() {}

    public JobResponseDTO(int id, String title, String description,
                          String location, double salary,
                          String recruiterName, String recruiterEmail,LocalDateTime date_time) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.recruiterName = recruiterName;
        this.recruiterEmail = recruiterEmail;
        this.postedAt =date_time;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }

    public LocalDateTime getPostedAt() {return postedAt;}
    public void setPostedAt(LocalDateTime postedAt) {this.postedAt = postedAt;}
}