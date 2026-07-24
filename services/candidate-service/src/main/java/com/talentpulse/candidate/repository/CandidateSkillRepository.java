package com.talentpulse.candidate.repository;

import com.talentpulse.candidate.entity.CandidateSkill;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, UUID> {

    List<CandidateSkill> findByCandidateProfileId(UUID candidateProfileId);
}
