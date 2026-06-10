package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.Job;
import com.sumit.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

    boolean existsByTitleAndCompanyAndRecruiter(String title, String company, User recruiter);

    @Query("SELECT j FROM Job j WHERE " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minSalary IS NULL OR j.salary >= :minSalary)")
    List<Job> searchJobs(@Param("title") String title,
                         @Param("location") String location,
                         @Param("minSalary") Integer minSalary);
}