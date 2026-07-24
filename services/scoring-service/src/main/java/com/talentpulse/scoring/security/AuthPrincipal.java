package com.talentpulse.scoring.security;

import com.talentpulse.scoring.enums.Role;
import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String email,
        Role role,
        UUID organizationId
) {
}
