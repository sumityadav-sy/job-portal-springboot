package com.sumit.jobportal.security;

import com.sumit.jobportal.entity.User;
import com.sumit.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service  // Spring manages this — it will auto-detect this implementation
          // of UserDetailsService and use it for authentication
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // Spring Security calls this method whenever it needs to load a user
    // "username" here means whatever identifier your app uses — we use email
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // 1. Fetch your User entity from the database using email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
        // UsernameNotFoundException is Spring Security's specific exception
        // for "user not found during authentication" — different from our
        // ResourceNotFoundException which is for general API 404s

        // 2. Convert your Role enum → Spring's GrantedAuthority
        // "ROLE_" prefix is Spring Security's convention
        // hasRole("RECRUITER") in @PreAuthorize checks for "ROLE_RECRUITER"
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        // user.getRole().name() → "RECRUITER" or "JOB_SEEKER"
        // after prefix:         → "ROLE_RECRUITER" or "ROLE_JOB_SEEKER"

        // 3. Build and return Spring's UserDetails object
        // org.springframework.security.core.userdetails.User (Spring's User class)
        // is a built-in implementation of UserDetails — we don't need to write our own
        //
        // Note: this is Spring's User class, NOT your com.sumit.jobportal.entity.User
        // They have the same name but live in different packages — be careful with imports
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())       // getUsername() will return email
                .password(user.getPassword())         // the BCrypt hash from DB
                .authorities(List.of(authority))      // ["ROLE_RECRUITER"]
                .accountExpired(false)                // account is active
                .accountLocked(false)                 // account is not locked
                .credentialsExpired(false)            // password hasn't expired
                .disabled(false)                      // account is enabled
                .build();
    }
}