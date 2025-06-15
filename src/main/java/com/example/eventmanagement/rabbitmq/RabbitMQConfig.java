package com.example.eventmanagement.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {


    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String mainQueue;

    @Value("${rabbitmq.dlq-queue}")
    private String dlqQueue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;  // main routing key

    @Value("${rabbitmq.routing-key-dlq}")
    private String dlqRoutingKey; //dlq routing key

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchange);
    }


    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(mainQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)    // DLQ routing key
                .withArgument("x-message-ttl", 60000) // Retry delay: 60 seconds
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(dlqQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", routingKey) // Main queue routing key
                .withArgument("x-message-ttl", 60000) // Retry delay: 60 seconds
                .build();
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(routingKey);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(notificationDlq())
                .to(notificationExchange())
                .with(dlqRoutingKey);
    }
}
