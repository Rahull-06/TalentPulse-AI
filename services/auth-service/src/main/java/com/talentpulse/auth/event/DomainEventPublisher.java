package com.talentpulse.auth.event;

public interface DomainEventPublisher {
    void publish(String routingKey, Object payload);
}
