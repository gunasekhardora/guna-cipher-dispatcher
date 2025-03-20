package org.guna.cipher.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.guna.cipher.dispatcher.service.DispatcherService;
import org.guna.cipher.model.CipherPayload;
import org.guna.cipher.model.Message;
import org.guna.cipher.model.MessageStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatcherServiceImpl implements DispatcherService {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Value("${kafka.topic.requests}")
    private String requestsTopic;

    // In-memory hash map implementation for message statuses (on a production system, this can be a database)
    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();

    @Override
    public String dispatchMessage(CipherPayload payload) {
        String uniqueId = UUID.randomUUID().toString();
        log.info("Dispatching a message with a uniqueId: {}", uniqueId);
        Message message = Message.builder()
                .payload(payload)
                .uniqueId(uniqueId)
                .retryCount(0)
                .status(MessageStatus.PENDING)
                .build();

        messageStore.put(uniqueId, message);

        kafkaTemplate.send(requestsTopic, uniqueId, message);
        log.info("Message dispatched to Kafka with uniqueId: {}", uniqueId);

        return uniqueId;
    }

    @Override
    public void updateMessageStatus(String uniqueId, MessageStatus status) {
        if (messageStore.containsKey(uniqueId)) {
            Message message = messageStore.get(uniqueId);
            message.setStatus(status);
            messageStore.put(uniqueId, message);
            log.info("Updated message status {} for uniqueId  {}", status, uniqueId);
        }
    }

    @Override
    public Message getMessageStatus(String messageId) {
        return messageStore.getOrDefault(messageId, null);
    }
}
