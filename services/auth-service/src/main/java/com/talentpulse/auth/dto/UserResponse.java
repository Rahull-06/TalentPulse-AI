package com.talentpulse.auth.dto;

import com.talentpulse.auth.enums.Role;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private UUID organizationId;
}
