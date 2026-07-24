package com.talentpulse.job.repository;

import com.talentpulse.job.entity.JobSkill;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, UUID> {
}
