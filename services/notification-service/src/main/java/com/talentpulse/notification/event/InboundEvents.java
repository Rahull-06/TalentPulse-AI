package com.talentpulse.notification.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Loose inbound DTOs — field names match publisher JSON; unknown type-ids are ignored via converter. */
public final class InboundEvents {

    private InboundEvents() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserRegistered {
        private UUID userId;
        private String fullName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class JobPublished {
        private UUID createdBy;
        private UUID jobId;
        private String title;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ApplicationCreated {
        private UUID candidateUserId;
        private UUID recruiterUserId;
        private UUID applicationId;
        private String jobTitle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ApplicationStatusChanged {
        private UUID candidateUserId;
        private UUID applicationId;
        private String toStatus;
        private String fromStatus;
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScoreCompleted {
        private UUID candidateUserId;
        private UUID applicationId;
        private BigDecimal fitScore;
    }
}
