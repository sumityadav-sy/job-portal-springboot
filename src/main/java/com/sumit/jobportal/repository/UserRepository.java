package com.sumit.jobportal.repository;

import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // find user by email — useful for login
    Optional<User> findByEmail(String email);

    // find all users by role
    List<User> findByRole(Role role);

    // check if email already registered
    boolean existsByEmail(String email);
}