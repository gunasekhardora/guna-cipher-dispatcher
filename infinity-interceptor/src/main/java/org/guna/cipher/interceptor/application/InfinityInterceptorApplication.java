package org.guna.cipher.interceptor.application;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication(scanBasePackages = "org.guna.cipher.interceptor")
public class InfinityInterceptorApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfinityInterceptorApplication.class, args);
    }
}
