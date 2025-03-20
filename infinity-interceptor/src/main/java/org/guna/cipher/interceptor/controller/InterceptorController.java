package org.guna.cipher.interceptor.controller;

import lombok.RequiredArgsConstructor;
import org.guna.cipher.interceptor.service.InterceptorService;
import org.guna.cipher.model.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/interceptor")
public class InterceptorController {

    private final InterceptorService interceptorService;

    @GetMapping("/messages")
    public ResponseEntity<Map<String, Message>> getAllMessages() {
        return ResponseEntity.ok(interceptorService.getAllMessages());
    }
}
