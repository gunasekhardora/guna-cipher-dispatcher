package org.guna.cipher.dispatcher.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.guna.cipher.dispatcher.service.DispatcherService;
import org.guna.cipher.model.CipherPayload;
import org.guna.cipher.model.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dispatch")
public class DispatcherController {

    private final DispatcherService dispatcherService;

    @PostMapping
    public ResponseEntity<String> dispatch(@Valid @RequestBody CipherPayload payload) {
        String messageId = dispatcherService.dispatchMessage(payload);
        return ResponseEntity.ok().body("Request dispatched with ID: " + messageId);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> receiveCallback(@RequestHeader("X-Unique-Id") String uniqueId) {
        log.info("Received callback for uniqueId: {}", uniqueId);
        return ResponseEntity.ok("Callback received for ID: " + uniqueId);
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<Message> getMessageStatus(@PathVariable String messageId) {
        Message message = dispatcherService.getMessageStatus(messageId);
        return ResponseEntity.ok(message);
    }

}
