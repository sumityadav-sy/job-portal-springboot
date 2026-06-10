package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

    // check if same recruiter already posted same title at same company
    boolean existsByTitleAndCompanyAndRecruiter(String title, String company, User recruiter);
}
