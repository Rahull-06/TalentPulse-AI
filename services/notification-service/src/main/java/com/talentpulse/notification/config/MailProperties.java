package com.talentpulse.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "talentpulse.mail")
public class MailProperties {

    /** When false, emails are logged only (no SMTP). */
    private boolean enabled;
    private String from;
}
