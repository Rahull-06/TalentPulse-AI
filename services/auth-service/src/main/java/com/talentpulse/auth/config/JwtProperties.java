package com.talentpulse.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Reads JWT settings from application.yml under talentpulse.security.jwt
 */
@Component
@ConfigurationProperties(prefix = "talentpulse.security.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiryMinutes;
    private long refreshTokenExpiryDays;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiryMinutes() {
        return accessTokenExpiryMinutes;
    }

    public void setAccessTokenExpiryMinutes(long accessTokenExpiryMinutes) {
        this.accessTokenExpiryMinutes = accessTokenExpiryMinutes;
    }

    public long getRefreshTokenExpiryDays() {
        return refreshTokenExpiryDays;
    }

    public void setRefreshTokenExpiryDays(long refreshTokenExpiryDays) {
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }
}
