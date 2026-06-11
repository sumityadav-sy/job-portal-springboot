package com.sumit.jobportal.service;

import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.exception.DuplicateResourceException;
import com.sumit.jobportal.exception.ResourceNotFoundException;
import com.sumit.jobportal.dto.UserRequestDTO;
import com.sumit.jobportal.dto.UserResponseDTO;
import com.sumit.jobportal.entity.Role;
import com.sumit.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public UserResponseDTO registerUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // map DTO → entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return convertToDTO(userRepository.save(user));
    }

    // READ ALL
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> convertToDTO(user))
                .toList();
    }

    // READ ONE
    public UserResponseDTO getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    // READ BY ROLE
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(user -> convertToDTO(user))
                .toList();
    }

    // DELETE
    public String deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return "User deleted successfully";
    }

    // Helper method to converting user to DTO
    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name() // enum → String e.g. "RECRUITER"
        );
    }
}