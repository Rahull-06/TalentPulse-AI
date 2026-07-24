package com.talentpulse.notification.security;

import com.talentpulse.notification.enums.Role;
import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String email,
        Role role,
        UUID organizationId
) {
}
