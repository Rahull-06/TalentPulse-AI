package com.talentpulse.gateway.web;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Wakes free-tier upstream services in parallel. Any HTTP response (including 401/500)
 * means the instance is up; only timeouts/connection errors count as still sleeping.
 */
@RestController
public class WarmupController {

    private final WebClient webClient;
    private final List<NamedUrl> targets;

    public WarmupController(
            @Value("${TALENTPULSE_AUTH_URL:http://localhost:8081}") String authUrl,
            @Value("${TALENTPULSE_JOB_URL:http://localhost:8082}") String jobUrl,
            @Value("${TALENTPULSE_CANDIDATE_URL:http://localhost:8083}") String candidateUrl,
            @Value("${TALENTPULSE_SCORING_URL:http://localhost:8084}") String scoringUrl,
            @Value("${TALENTPULSE_NOTIFICATION_URL:http://localhost:8085}") String notificationUrl
    ) {
        this.webClient = WebClient.builder().build();
        this.targets = List.of(
                new NamedUrl("auth", trimSlash(authUrl)),
                new NamedUrl("job", trimSlash(jobUrl)),
                new NamedUrl("candidate", trimSlash(candidateUrl)),
                new NamedUrl("scoring", trimSlash(scoringUrl)),
                new NamedUrl("notification", trimSlash(notificationUrl))
        );
    }

    @GetMapping(value = "/api/v1/system/warmup", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> warmup() {
        return Flux.fromIterable(targets)
                .flatMap(target -> ping(target).map(status -> Map.entry(target.name(), status)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue, LinkedHashMap::new)
                .map(services -> {
                    boolean ready = services.values().stream().noneMatch("down"::equals);
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("ready", ready);
                    body.put("services", services);
                    return body;
                });
    }

    private Mono<String> ping(NamedUrl target) {
        // Any HTTP response means the instance is awake (401/500 still count as up).
        return webClient.get()
                .uri(target.baseUrl() + "/actuator/health")
                .exchangeToMono(response -> response.releaseBody().thenReturn("up"))
                .timeout(Duration.ofSeconds(90))
                .onErrorReturn("down");
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private record NamedUrl(String name, String baseUrl) {
    }
}
