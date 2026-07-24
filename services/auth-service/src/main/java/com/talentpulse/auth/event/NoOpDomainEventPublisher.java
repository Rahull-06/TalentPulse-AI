package com.talentpulse.auth.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talentpulse.events.enabled", havingValue = "false")
@Slf4j
public class NoOpDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(String routingKey, Object payload) {
        log.debug("Events disabled — skipped {}", routingKey);
    }
}
