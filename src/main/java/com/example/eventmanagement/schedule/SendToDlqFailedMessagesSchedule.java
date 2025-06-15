package com.example.eventmanagement.schedule;

import com.example.eventmanagement.entity.FailedMessage;
import com.example.eventmanagement.rabbitmq.AmqpMessageDeserializer;
import com.example.eventmanagement.repository.FailedMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendToDlqFailedMessagesSchedule {

    private final FailedMessageRepository failedMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key-dlq}")
    private String dlqRoutingKey;

    @Scheduled(fixedDelay = 60000*60*2) // Every 60 seconds * 60 *2 (2hr)
    public void retryUnsentMessages() {
        log.info("SendToDlqFailedMessagesSchedule is running");
        List<FailedMessage> failedMessages = failedMessageRepository.findBySendToDlqFalse();

        log.info("Found {} failed messages to retry sending dlq...", failedMessages.size());

        for (FailedMessage failed : failedMessages) {
            try {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Message.class, new AmqpMessageDeserializer());

                ObjectMapper customMapper = new ObjectMapper();
                customMapper.registerModule(module);

                Message message = customMapper.readValue(failed.getPayLoad(), Message.class);

                rabbitTemplate.send(exchange, dlqRoutingKey, message);
                failed.setSendToDlq(true);

                log.info("Successfully resent failed message with ID {} to DLQ", failed.getId());

            } catch (Exception e) {
                log.error("Retry to DLQ failed for message ID {}: {}", failed.getId(), e.getMessage());
                failed.setRetryAttempts(failed.getRetryAttempts() + 1);
                failed.setSendToDlq(false);
            }

            failedMessageRepository.save(failed);
        }
    }


}
