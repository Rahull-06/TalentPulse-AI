package com.talentpulse.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void contextLoadsAndRoutesRegistered() {
        List<String> routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertThat(routeIds).isNotNull();
        assertThat(routeIds).contains(
                "auth-service",
                "job-service",
                "candidate-service-job-applications",
                "candidate-service-profiles",
                "candidate-service-applications",
                "scoring-service",
                "notification-service"
        );
    }
}
