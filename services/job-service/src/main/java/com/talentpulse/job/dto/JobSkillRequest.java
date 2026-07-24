package com.talentpulse.job.dto;

import com.talentpulse.job.enums.SkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSkillRequest {

    @NotBlank
    @Size(max = 100)
    private String skillName;

    @NotNull
    private SkillType skillType;

    @Min(1)
    @Max(10)
    private Integer weight = 1;
}
