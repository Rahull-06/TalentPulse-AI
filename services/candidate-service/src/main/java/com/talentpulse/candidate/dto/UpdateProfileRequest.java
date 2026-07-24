package com.talentpulse.candidate.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 200)
    private String headline;

    private String summary;

    private Integer experienceYears;

    @Size(max = 150)
    private String location;

    @Size(max = 255)
    private String linkedinUrl;

    @Size(max = 255)
    private String githubUrl;

    @Size(max = 30)
    private String phone;
}
