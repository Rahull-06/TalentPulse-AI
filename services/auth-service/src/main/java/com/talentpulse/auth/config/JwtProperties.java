package com.talentpulse.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Reads JWT settings from application.yml under talentpulse.security.jwt
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "talentpulse.security.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiryMinutes;
    private long refreshTokenExpiryDays;
}
