package com.talentpulse.job.dto;

import com.talentpulse.job.enums.EmploymentType;
import com.talentpulse.job.enums.JobStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobResponse {

    private UUID id;
    private UUID organizationId;
    private UUID createdBy;
    private String title;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private Integer experienceMin;
    private Integer experienceMax;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private JobStatus status;
    private Integer openings;
    private Integer maxApplicants;
    private Instant publishedAt;
    private Instant closedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<JobSkillResponse> skills;
}
