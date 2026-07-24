package com.talentpulse.scoring.repository;

import com.talentpulse.scoring.entity.ScoreResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreResultRepository extends JpaRepository<ScoreResult, UUID> {

    Optional<ScoreResult> findByApplicationId(UUID applicationId);

    Page<ScoreResult> findByJobIdAndOrganizationIdOrderByFitScoreDesc(
            UUID jobId,
            UUID organizationId,
            Pageable pageable
    );

    boolean existsByApplicationId(UUID applicationId);
}
