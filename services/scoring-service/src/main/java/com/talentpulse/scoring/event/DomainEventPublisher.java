package com.talentpulse.scoring.event;

public interface DomainEventPublisher {
    void publish(String routingKey, Object payload);
}
