package com.talentpulse.candidate.repository;

import com.talentpulse.candidate.entity.Resume;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByCandidateProfileIdOrderByUploadedAtDesc(UUID candidateProfileId);

    Optional<Resume> findByIdAndCandidateProfileId(UUID id, UUID candidateProfileId);

    Optional<Resume> findByCandidateProfileIdAndPrimaryResumeTrue(UUID candidateProfileId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Resume r
            SET r.primaryResume = false
            WHERE r.candidateProfile.id = :profileId AND r.primaryResume = true
            """)
    int clearPrimaryFlag(@Param("profileId") UUID profileId);
}
