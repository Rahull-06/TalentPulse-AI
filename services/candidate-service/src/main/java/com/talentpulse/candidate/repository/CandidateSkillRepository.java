package com.talentpulse.candidate.repository;

import com.talentpulse.candidate.entity.CandidateSkill;
import com.talentpulse.candidate.enums.SkillSource;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, UUID> {

    List<CandidateSkill> findByCandidateProfileId(UUID candidateProfileId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CandidateSkill s where s.candidateProfile.id = :profileId and s.source = :source")
    void deleteByCandidateProfileIdAndSource(
            @Param("profileId") UUID profileId,
            @Param("source") SkillSource source
    );
}
