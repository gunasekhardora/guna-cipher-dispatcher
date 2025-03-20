package org.guna.cipher.interceptor.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.guna.cipher.model.Message;
import org.guna.cipher.model.MessageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InterceptorService {
    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();
    private KafkaConsumer<String, Message> consumer;
    private final ConsumerFactory<String, Message> consumerFactory;

    @Value("${kafka.topic.requests}")
    private String requestsTopic;

    @Value("${cipher.dispatcher.callback-url}")
    private String callbackUrl;

    @Autowired
    public InterceptorService(KafkaTemplate<String, Message> kafkaTemplate, RestTemplate restTemplate, 
                             ConsumerFactory<String, Message> consumerFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
        this.consumerFactory = consumerFactory;
    }
    
    @PostConstruct
    public void init() {
        this.consumer = (KafkaConsumer<String, Message>) consumerFactory.createConsumer();
        this.consumer.subscribe(Collections.singletonList(requestsTopic));
    }
    
    @PreDestroy
    public void cleanup() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    public void pollMessages() {
        log.info("Polling for messages from topic after a 5 sec gap : {}", requestsTopic);
        try {
            ConsumerRecords<String, Message> records = consumer.poll(Duration.ofMillis(1000));
            log.info("Received {} records", records.count());
            
            if (records.isEmpty()) {
                return;
            }
            
            Map<TopicPartition, Long> offsetsToCommit = new ConcurrentHashMap<>();
            
            records.forEach(record -> {
                String messageId = record.key();
                Message message = record.value();
                TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                
                log.info("Processing message with ID: {}", messageId);
                
                // Skipping if message is processed already (handling at-most-once)
                if (messageStore.containsKey(messageId) &&
                        messageStore.get(messageId).getStatus() == MessageStatus.SUCCESS) {
                    log.info("Message {} already processed successfully, skipping", messageId);
                    offsetsToCommit.put(partition, record.offset() + 1);
                    return;
                }
                
                message.setStatus(MessageStatus.PROCESSING);
                messageStore.put(messageId, message);
                
                try {
                    // to simulate 40% failure rate
                    Random random = new Random();
                    boolean shouldFail = random.nextDouble() < 0.4;
                    
                    if (!shouldFail) {
                        message.setStatus(MessageStatus.SUCCESS);
                        messageStore.put(messageId, message);
                        sendCallback(message.getUniqueId());
                        log.info("Successfully processed message {}", messageId);
                        offsetsToCommit.put(partition, record.offset() + 1);
                    } else {
                        log.error("Failed to process message {}", messageId);
                        
                        if (message.getRetryCount() < 1) {
                            // Retry once
                            message.setRetryCount(message.getRetryCount() + 1);
                            message.setStatus(MessageStatus.RETRY);
                            messageStore.put(messageId, message);
                            
                            // Re-publish for retry
                            kafkaTemplate.send(requestsTopic, messageId, message);
                            log.info("Republished message {} for retry", messageId);
                            offsetsToCommit.put(partition, record.offset() + 1);
                        } else {
                            // Mark as failed after retry
                            message.setStatus(MessageStatus.FAILED);
                            messageStore.put(messageId, message);
                            log.error("Message {} failed after retry", messageId);
                            offsetsToCommit.put(partition, record.offset() + 1);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing message {}: {}", messageId, e.getMessage());
                    message.setStatus(MessageStatus.FAILED);
                    messageStore.put(messageId, message);
                    offsetsToCommit.put(partition, record.offset() + 1);
                }
            });
            
            if (!offsetsToCommit.isEmpty()) {
                try {
                    for (Map.Entry<TopicPartition, Long> entry : offsetsToCommit.entrySet()) {
                        consumer.commitSync(Collections.singletonMap(entry.getKey(), 
                                new org.apache.kafka.clients.consumer.OffsetAndMetadata(entry.getValue())));
                    }
                    log.info("Committed offsets for {} partitions", offsetsToCommit.size());
                } catch (Exception e) {
                    log.error("Failed to commit offsets: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during message polling: {}", e.getMessage());
        }
    }

    private void sendCallback(String uniqueId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Unique-Id", uniqueId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(callbackUrl, HttpMethod.GET, entity, String.class);
            log.info("Sent callback for uniqueId: {}, Response: {}", uniqueId, response.getBody());
        } catch (Exception e) {
            log.error("Failed to send callback for uniqueId {}: {}", uniqueId, e.getMessage());
        }
    }

    public Map<String, Message> getAllMessages() {
        return messageStore;
    }
}
