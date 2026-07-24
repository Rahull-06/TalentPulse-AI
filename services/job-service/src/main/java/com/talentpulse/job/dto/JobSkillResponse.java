package com.talentpulse.job.dto;

import com.talentpulse.job.enums.SkillType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobSkillResponse {

    private UUID id;
    private String skillName;
    private SkillType skillType;
    private Integer weight;
}
