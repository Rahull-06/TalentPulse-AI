package com.talentpulse.auth.security;

import com.talentpulse.auth.enums.Role;
import java.util.UUID;

/**
 * Who is calling the API (taken from JWT claims).
 * Stored as the "principal" in Spring SecurityContext.
 */
public record AuthPrincipal(
        UUID userId,
        String email,
        Role role,
        UUID organizationId
) {
}
