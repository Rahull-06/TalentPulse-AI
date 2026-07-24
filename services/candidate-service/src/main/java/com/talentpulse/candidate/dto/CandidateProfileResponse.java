package com.talentpulse.candidate.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandidateProfileResponse {

    private UUID id;
    private UUID userId;
    private String headline;
    private String summary;
    private Integer experienceYears;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String phone;
    private List<CandidateSkillResponse> skills;
    private Instant createdAt;
    private Instant updatedAt;
}
