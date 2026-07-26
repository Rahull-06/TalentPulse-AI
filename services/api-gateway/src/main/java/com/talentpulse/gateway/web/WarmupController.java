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
 * Kicks free-tier upstream services awake. Returns immediately so Render's
 * proxy / browsers never time out; pings continue in the background.
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
        // Fire-and-forget: do not block the HTTP response on cold starts.
        Flux.fromIterable(targets)
                .flatMap(this::ping)
                .subscribe();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("started", true);
        body.put("services", targets.stream().map(NamedUrl::name).toList());
        return Mono.just(body);
    }

    private Mono<String> ping(NamedUrl target) {
        return webClient.get()
                .uri(target.baseUrl() + "/actuator/health")
                .exchangeToMono(response -> response.releaseBody().thenReturn("up"))
                .timeout(Duration.ofSeconds(90))
                .onErrorReturn("down")
                .doOnNext(status -> {
                    // no-op; background wake only
                });
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
