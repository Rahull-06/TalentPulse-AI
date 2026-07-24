package com.talentpulse.job.entity;

import com.talentpulse.job.enums.EmploymentType;
import com.talentpulse.job.enums.JobStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A job opening belonging to one organization (tenant).
 * organizationId / createdBy come from Auth JWT — not a local User table.
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    /** Tenant isolation — from recruiter's JWT organizationId */
    @Column(nullable = false)
    private UUID organizationId;

    /** Recruiter user id from Auth Service */
    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 150)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    @Column(nullable = false)
    private Integer experienceMin;

    @Column(nullable = false)
    private Integer experienceMax;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    private Instant publishedAt;

    private Instant closedAt;

    /** How many people to hire for this role (optional). */
    private Integer openings;

    /** Cap on applications; null means unlimited. */
    private Integer maxApplicants;

    /**
     * One job has many skills.
     * cascade ALL = saving/deleting job also saves/deletes skills.
     * orphanRemoval = removing a skill from the list deletes it in DB.
     */
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobSkill> skills = new ArrayList<>();

    public void addSkill(JobSkill skill) {
        skills.add(skill);
        skill.setJob(this);
    }

    public void clearSkills() {
        skills.forEach(skill -> skill.setJob(null));
        skills.clear();
    }
}
