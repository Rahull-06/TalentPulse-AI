package com.talentpulse.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Job Service entry point.
 * Owns job posts + required/preferred skills for each organization.
 */
@SpringBootApplication
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
