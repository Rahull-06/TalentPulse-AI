package com.talentpulse.candidate.entity;

import com.talentpulse.candidate.enums.ParseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    private CandidateProfile candidateProfile;

    @Column(nullable = false, length = 255)
    private String fileName;

    /** Local path or future S3 URL */
    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false, length = 20)
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String parsedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParseStatus parseStatus;

    @Column(nullable = false)
    private boolean primaryResume;

    @Column(nullable = false)
    private Instant uploadedAt;
}
