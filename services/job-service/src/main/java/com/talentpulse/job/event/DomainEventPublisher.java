package com.talentpulse.job.event;

public interface DomainEventPublisher {
    void publish(String routingKey, Object payload);
}
