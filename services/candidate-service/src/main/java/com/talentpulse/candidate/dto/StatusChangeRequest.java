package com.talentpulse.candidate.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusChangeRequest {

    @Size(max = 2000)
    private String note;
}
