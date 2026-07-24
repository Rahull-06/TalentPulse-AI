package com.talentpulse.job.dto;

import com.talentpulse.job.enums.EmploymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJobRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 150)
    private String location;

    @NotNull
    private EmploymentType employmentType;

    @NotNull
    @Min(0)
    private Integer experienceMin;

    @NotNull
    @Min(0)
    private Integer experienceMax;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Size(max = 10)
    private String currency;

    @Min(1)
    private Integer openings;

    @Min(1)
    private Integer maxApplicants;

    @NotEmpty
    @Valid
    private List<JobSkillRequest> skills;

    @AssertTrue(message = "experienceMax must be >= experienceMin")
    public boolean isExperienceRangeValid() {
        if (experienceMin == null || experienceMax == null) {
            return true;
        }
        return experienceMax >= experienceMin;
    }
}
