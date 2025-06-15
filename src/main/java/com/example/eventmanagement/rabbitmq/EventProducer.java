package com.example.eventmanagement.rabbitmq;

import com.example.eventmanagement.dto.request.FailedMessageRequestDto;
import com.example.eventmanagement.service.FailedMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final FailedMessageService failedMessageService;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;  // main queue routing key

    @Value("${rabbitmq.routing-key-dlq}")
    private String dlqRoutingKey;  // DLQ routing key

    @Value("${rabbitmq.max-attempts}")
    private Integer maxRetryAttempts;



    @Async
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "rabbitSender", fallbackMethod = "sendToRetryQueue")
    public void sendEvent(Message message) throws Exception {
        int attempt = 0;
        boolean sent = false;

        while (attempt < maxRetryAttempts && !sent) {
            try {
                log.info("Message sent to exchange {} with routing key {} message.getBody() {}", exchange, routingKey, message.getBody().toString());
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("rabbitSender");
                log.info("CircuitBreaker state: {}", cb.getState());
                rabbitTemplate.send(exchange, routingKey, message);
                sent = true;
                log.info("Event sent  on attempt {}", attempt + 1);
            } catch (Exception ex) {
                log.error("Event sending error", ex);
                attempt++;
                log.warn("Attempt {} to send Event to Rabbit MQ failed: {}", attempt,  ex.getMessage());
                if (attempt >= maxRetryAttempts) {
                    throw ex; // triggers circuit breaker fallback
                }
            }
        }
    }

    /**
     * Circuit breaker fallback method after maxAttempts failures
     */
    public void sendToRetryQueue(Message message, Throwable ex) throws JsonProcessingException {

        MessageProperties props = message.getMessageProperties();
        String messageContent = objectMapper.writeValueAsString(message);
        boolean sendtoDlq = false;

        log.error("Event sending failed for RabbitMq permanently. send to DLQ for future usage");

        try {
            rabbitTemplate.send(exchange, dlqRoutingKey, message); // \attempt to send to DLQ
            sendtoDlq = true;
        } catch (Exception e) {
            log.error("Even DLQ failed: {}", e.getMessage());
        }

        log.error("Event sending failed for RabbitMq permanently. Storing in DB. Error: {}", ex.getMessage());
        failedMessageService.saveFailedMessage(
                new FailedMessageRequestDto(null,
                        (String) props.getHeaders().getOrDefault("title", "No title"),
                        "Exceeded retry attempts",
                        maxRetryAttempts,
                        messageContent, null, null, sendtoDlq, maxRetryAttempts
                )
        );
    }


}
