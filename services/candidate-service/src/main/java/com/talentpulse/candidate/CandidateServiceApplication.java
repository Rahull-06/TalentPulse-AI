package com.talentpulse.candidate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Candidate Service entry point.
 * Owns profiles, resumes, and job applications.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CandidateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandidateServiceApplication.class, args);
    }
}
