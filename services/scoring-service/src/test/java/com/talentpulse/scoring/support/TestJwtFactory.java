package com.talentpulse.scoring.support;

import com.talentpulse.scoring.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public final class TestJwtFactory {

    private static final String SECRET = "TestOnlySecretKeyMustBeAtLeast32Characters!";

    private TestJwtFactory() {
    }

    public static String recruiterToken(UUID userId, UUID organizationId) {
        return token(userId, "recruiter@test.com", Role.RECRUITER, organizationId);
    }

    public static String candidateToken(UUID userId) {
        return token(userId, "candidate@test.com", Role.CANDIDATE, null);
    }

    private static String token(UUID userId, String email, Role role, UUID organizationId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key);
        if (organizationId != null) {
            builder.claim("organizationId", organizationId.toString());
        }
        return builder.compact();
    }
}
