package com.talentpulse.candidate.dto;

import com.talentpulse.candidate.enums.ParseStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeResponse {

    private UUID id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private ParseStatus parseStatus;
    private boolean primaryResume;
    private Instant uploadedAt;
}
