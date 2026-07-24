package com.talentpulse.job.entity;

import com.talentpulse.job.enums.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A skill attached to a job (e.g. Java REQUIRED weight 5).
 */
@Entity
@Table(name = "job_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, length = 100)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillType skillType;

    /** Higher weight = more important for scoring later */
    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 1;
}
