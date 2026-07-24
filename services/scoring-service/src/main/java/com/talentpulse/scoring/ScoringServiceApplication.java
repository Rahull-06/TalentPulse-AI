package com.talentpulse.scoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scoring Service entry point.
 * Produces explainable fit scores + interview questions.
 * AI assists; never auto-hires or auto-rejects.
 */
@SpringBootApplication
public class ScoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoringServiceApplication.class, args);
    }
}
