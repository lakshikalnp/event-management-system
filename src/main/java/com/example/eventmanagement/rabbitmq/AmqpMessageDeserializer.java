package com.example.eventmanagement.rabbitmq;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
public class AmqpMessageDeserializer extends JsonDeserializer<Message> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.getCodec().readTree(p);

        JsonNode messagePropsNode = root.get("messageProperties");
        JsonNode bodyNode = root.get("body");

        // Deserialize messageProperties
        MessageProperties messageProperties = mapper.treeToValue(messagePropsNode, MessageProperties.class);

        // Decode base64-encoded body
        byte[] body = Base64.getDecoder().decode(bodyNode.asText());

        return new Message(body, messageProperties);
    }

}
