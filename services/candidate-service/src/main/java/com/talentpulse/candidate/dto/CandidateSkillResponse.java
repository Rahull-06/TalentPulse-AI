package com.talentpulse.candidate.dto;

import com.talentpulse.candidate.enums.Proficiency;
import com.talentpulse.candidate.enums.SkillSource;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandidateSkillResponse {

    private UUID id;
    private String skillName;
    private Proficiency proficiency;
    private SkillSource source;
}
