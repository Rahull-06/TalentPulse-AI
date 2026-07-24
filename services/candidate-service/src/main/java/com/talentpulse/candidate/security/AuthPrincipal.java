package com.talentpulse.candidate.security;

import com.talentpulse.candidate.enums.Role;
import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String email,
        Role role,
        UUID organizationId
) {
}
