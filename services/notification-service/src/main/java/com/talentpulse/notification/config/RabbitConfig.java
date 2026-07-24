package com.talentpulse.notification.config;

import com.talentpulse.notification.event.EventKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "talentpulse.events.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitConfig {

    @Bean
    TopicExchange talentPulseExchange() {
        return new TopicExchange(EventKeys.EXCHANGE, true, false);
    }

    @Bean
    Queue userRegisteredQueue() {
        return new Queue(EventKeys.Q_USER_REGISTERED, true);
    }

    @Bean
    Queue jobPublishedQueue() {
        return new Queue(EventKeys.Q_JOB_PUBLISHED, true);
    }

    @Bean
    Queue applicationCreatedQueue() {
        return new Queue(EventKeys.Q_APPLICATION_CREATED, true);
    }

    @Bean
    Queue statusChangedQueue() {
        return new Queue(EventKeys.Q_STATUS_CHANGED, true);
    }

    @Bean
    Queue scoreCompletedQueue() {
        return new Queue(EventKeys.Q_SCORE_COMPLETED, true);
    }

    @Bean
    Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange talentPulseExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(talentPulseExchange).with(EventKeys.USER_REGISTERED);
    }

    @Bean
    Binding jobPublishedBinding(Queue jobPublishedQueue, TopicExchange talentPulseExchange) {
        return BindingBuilder.bind(jobPublishedQueue).to(talentPulseExchange).with(EventKeys.JOB_PUBLISHED);
    }

    @Bean
    Binding applicationCreatedBinding(Queue applicationCreatedQueue, TopicExchange talentPulseExchange) {
        return BindingBuilder.bind(applicationCreatedQueue)
                .to(talentPulseExchange)
                .with(EventKeys.APPLICATION_CREATED);
    }

    @Bean
    Binding statusChangedBinding(Queue statusChangedQueue, TopicExchange talentPulseExchange) {
        return BindingBuilder.bind(statusChangedQueue)
                .to(talentPulseExchange)
                .with(EventKeys.APPLICATION_STATUS_CHANGED);
    }

    @Bean
    Binding scoreCompletedBinding(Queue scoreCompletedQueue, TopicExchange talentPulseExchange) {
        return BindingBuilder.bind(scoreCompletedQueue).to(talentPulseExchange).with(EventKeys.SCORE_COMPLETED);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        // Ignore publisher __TypeId__ (other services' package names) — use listener method type.
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }
}
