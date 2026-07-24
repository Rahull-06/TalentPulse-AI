package com.talentpulse.scoring.entity;

import com.talentpulse.scoring.enums.ScoringMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Explainable fit score for one application.
 * One latest score per application (unique applicationId).
 */
@Entity
@Table(
        name = "score_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_score_application",
                columnNames = "application_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreResult extends BaseEntity {

    @Column(nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private UUID jobId;

    @Column(nullable = false)
    private UUID organizationId;

    /** 0.00 – 100.00 */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal fitScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScoringMode scoringMode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @Builder.Default
    private List<String> matchedSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @Builder.Default
    private List<String> missingSkills = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String resumeSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 100)
    private String modelName;
}
