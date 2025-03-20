package org.guna.cipher.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private CipherPayload payload;
    private String uniqueId;
    private int retryCount;
    private MessageStatus status;
}
