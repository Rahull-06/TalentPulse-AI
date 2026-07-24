package com.talentpulse.job.security;

import com.talentpulse.job.enums.Role;
import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String email,
        Role role,
        UUID organizationId
) {
}
