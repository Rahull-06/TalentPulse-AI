package com.talentpulse.scoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * v1: caller sends skills/text so Scoring works without cross-service calls yet.
 * Later: Scoring can fetch from Job/Candidate services.
 */
@Getter
@Setter
public class ComputeScoreRequest {

    @NotNull
    private UUID applicationId;

    @NotNull
    private UUID jobId;

    @NotNull
    private UUID organizationId;

    @NotEmpty
    private List<@NotBlank String> requiredSkills;

    private List<String> preferredSkills;

    @NotEmpty
    private List<@NotBlank String> candidateSkills;

    private String resumeText;

    private String jobTitle;
}
