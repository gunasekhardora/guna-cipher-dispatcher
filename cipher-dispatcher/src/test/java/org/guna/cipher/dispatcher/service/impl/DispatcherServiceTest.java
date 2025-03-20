package org.guna.cipher.dispatcher.service.impl;

import org.guna.cipher.model.CipherPayload;
import org.guna.cipher.model.Message;
import org.guna.cipher.model.MessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DispatcherServiceTest {

    @Mock
    private KafkaTemplate<String, Message> kafkaTemplate;

    @InjectMocks
    private DispatcherServiceImpl dispatcherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dispatcherService, "requestsTopic", "test-topic");
    }

    @Test
    void dispatchMessage_ShouldCreateAndDispatchMessage() {
        CipherPayload payload = new CipherPayload();
        payload.setName("Test Name");

        String uniqueId = dispatcherService.dispatchMessage(payload);

        assertNotNull(uniqueId);
        verify(kafkaTemplate).send(eq("test-topic"), eq(uniqueId), any(Message.class));

        Message storedMessage = dispatcherService.getMessageStatus(uniqueId);
        assertNotNull(storedMessage);
        assertEquals(MessageStatus.PENDING, storedMessage.getStatus());
        assertEquals(0, storedMessage.getRetryCount());
        assertEquals(payload, storedMessage.getPayload());
    }

    @Test
    void updateMessageStatus_ShouldUpdateStatus() {
        CipherPayload payload = new CipherPayload();
        String uniqueId = dispatcherService.dispatchMessage(payload);

        dispatcherService.updateMessageStatus(uniqueId, MessageStatus.SUCCESS);

        Message updatedMessage = dispatcherService.getMessageStatus(uniqueId);
        assertEquals(MessageStatus.SUCCESS, updatedMessage.getStatus());
    }

    @Test
    void getMessageStatus_WithNonExistentId_ShouldReturnNull() {
        Message message = dispatcherService.getMessageStatus("non-existent-id");
        assertNull(message);
    }
}