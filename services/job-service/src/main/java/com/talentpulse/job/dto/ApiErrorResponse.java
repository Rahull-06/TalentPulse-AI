package com.talentpulse.job.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

    private boolean success;
    private String message;
    private List<FieldErrorItem> errors;
    private Instant timestamp;

    @Getter
    @AllArgsConstructor
    public static class FieldErrorItem {
        private String field;
        private String message;
    }
}
