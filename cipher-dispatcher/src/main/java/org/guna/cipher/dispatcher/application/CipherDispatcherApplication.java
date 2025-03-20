package org.guna.cipher.dispatcher.application;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@OpenAPIDefinition
@EnableScheduling
@ComponentScan(basePackages = {"org.guna.cipher.*"})
@SpringBootApplication
public class CipherDispatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(CipherDispatcherApplication.class, args);
    }
}
