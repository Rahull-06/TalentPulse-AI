package com.talentpulse.auth.dto;

import com.talentpulse.auth.entity.User;

/**
 * Converts Entity → Response DTO (never expose passwordHash).
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .build();
    }
}
