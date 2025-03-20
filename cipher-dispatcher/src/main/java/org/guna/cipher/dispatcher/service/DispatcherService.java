package org.guna.cipher.dispatcher.service;

import org.guna.cipher.model.CipherPayload;
import org.guna.cipher.model.Message;
import org.guna.cipher.model.MessageStatus;

public interface DispatcherService {
    String dispatchMessage(CipherPayload payload);
    void updateMessageStatus(String uniqueId, MessageStatus status);
    Message getMessageStatus(String messageId);
}
