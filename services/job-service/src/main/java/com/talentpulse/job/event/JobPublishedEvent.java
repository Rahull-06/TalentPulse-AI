package com.talentpulse.job.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPublishedEvent {
    private UUID jobId;
    private UUID organizationId;
    private UUID createdBy;
    private String title;
}
