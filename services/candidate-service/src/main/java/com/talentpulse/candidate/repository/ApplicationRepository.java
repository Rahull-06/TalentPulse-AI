package com.talentpulse.candidate.repository;

import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.enums.ApplicationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByJobIdAndCandidateProfileId(UUID jobId, UUID candidateProfileId);

    Optional<Application> findByIdAndCandidateProfileId(UUID id, UUID candidateProfileId);

    Page<Application> findByCandidateProfileId(UUID candidateProfileId, Pageable pageable);

    Page<Application> findByJobIdAndOrganizationId(
            UUID jobId,
            UUID organizationId,
            Pageable pageable
    );

    Page<Application> findByJobIdAndOrganizationIdAndStatus(
            UUID jobId,
            UUID organizationId,
            ApplicationStatus status,
            Pageable pageable
    );

    Optional<Application> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByJobId(UUID jobId);
}
