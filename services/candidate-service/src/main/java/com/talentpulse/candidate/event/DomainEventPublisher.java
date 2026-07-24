package com.talentpulse.candidate.event;

public interface DomainEventPublisher {

    void publish(String routingKey, Object payload);
}
