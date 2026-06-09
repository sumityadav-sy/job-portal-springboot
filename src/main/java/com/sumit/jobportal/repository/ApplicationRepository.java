package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.Application;
import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    // Replaces: SELECT * FROM applications WHERE user_id = ?
    List<Application> findByApplicant(User applicant);

    // Replaces: SELECT * FROM applications WHERE job_id = ?
    List<Application> findByJob(Job job);

    // Replaces: SELECT 1 FROM applications WHERE job_id = ? AND user_id = ?
    boolean existsByApplicantAndJob(User applicant, Job job);
}