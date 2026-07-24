package com.talentpulse.scoring.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talentpulse.events.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RabbitDomainEventPublisher implements DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String routingKey, Object payload) {
        log.info("Publishing event key={} type={}", routingKey, payload.getClass().getSimpleName());
        try {
            rabbitTemplate.convertAndSend(EventKeys.EXCHANGE, routingKey, payload);
        } catch (Exception ex) {
            log.error("Failed to publish event key={} — check RabbitMQ", routingKey, ex);
        }
    }
}
