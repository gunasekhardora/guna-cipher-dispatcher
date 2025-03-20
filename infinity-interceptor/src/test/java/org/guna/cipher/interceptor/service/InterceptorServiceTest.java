package org.guna.cipher.interceptor.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.guna.cipher.model.Message;
import org.guna.cipher.model.MessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterceptorServiceTest {

    @Mock
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConsumerFactory<String, Message> consumerFactory;

    @Mock
    private KafkaConsumer<String, Message> consumer;

    @InjectMocks
    private InterceptorService interceptorService;

    private final String TOPIC = "test-topic";
    private final TopicPartition topicPartition = new TopicPartition(TOPIC, 0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(interceptorService, "requestsTopic", TOPIC);
        ReflectionTestUtils.setField(interceptorService, "callbackUrl", "http://test-callback");
        ReflectionTestUtils.setField(interceptorService, "consumer", consumer);
        
        when(consumerFactory.createConsumer()).thenReturn(consumer);
        interceptorService.init();
    }

    @Test
    void pollMessages_SuccessfulProcessing() {
        Message message = Message.builder()
                .uniqueId("test-id")
                .status(MessageStatus.PENDING)
                .retryCount(0)
                .build();

        ConsumerRecord<String, Message> record = new ConsumerRecord<>(TOPIC, 0, 0L, "test-key", message);
        ConsumerRecords<String, Message> records = new ConsumerRecords<>(
                Collections.singletonMap(topicPartition, Collections.singletonList(record)));

        when(consumer.poll(any(Duration.class))).thenReturn(records);

        interceptorService.pollMessages();

        Message storedMessage = interceptorService.getAllMessages().get("test-key");
        assertNotNull(storedMessage);
        verify(consumer).commitSync(any(Map.class));
    }

    @Test
    void pollMessages_EmptyRecords() {
        ConsumerRecords<String, Message> emptyRecords = new ConsumerRecords<>(new HashMap<>());
        when(consumer.poll(any(Duration.class))).thenReturn(emptyRecords);

        interceptorService.pollMessages();

        verify(consumer, never()).commitSync(any(Map.class));
    }

    @Test
    void getAllMessages_ReturnsMessageStore() {
        Message message = Message.builder()
                .uniqueId("test-id")
                .status(MessageStatus.SUCCESS)
                .build();
        ReflectionTestUtils.setField(interceptorService, "messageStore", 
                Collections.singletonMap("test-key", message));

        Map<String, Message> result = interceptorService.getAllMessages();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(message, result.get("test-key"));
    }

    @Test
    void getAllMessages_EmptyStore() {
        Map<String, Message> result = interceptorService.getAllMessages();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}