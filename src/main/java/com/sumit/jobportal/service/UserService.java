package com.sumit.jobportal.service;

import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public User registerUser(User user) {
        // business rule: no duplicate emails
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // READ ALL
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ ONE
    public User getUserById(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    // READ BY ROLE
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // DELETE
    public String deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return "User deleted successfully";
    }
}