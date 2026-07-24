package com.talentpulse.scoring.repository;

import com.talentpulse.scoring.entity.InterviewQuestionSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionSetRepository extends JpaRepository<InterviewQuestionSet, UUID> {

    Optional<InterviewQuestionSet> findFirstByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
