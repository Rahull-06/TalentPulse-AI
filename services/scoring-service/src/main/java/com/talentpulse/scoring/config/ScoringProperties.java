package com.talentpulse.scoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "talentpulse.scoring")
public class ScoringProperties {

    private boolean aiEnabled;
    private Gemini gemini = new Gemini();

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String model;
    }
}
