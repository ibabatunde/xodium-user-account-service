package com.xodium.useraccountservice.kafka.service;

import com.xodium.useraccountservice.kafka.dto.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topics
    private static final String USER_TOPIC = "user-registered-events";

    public void publishUserRegistrationEvent(UserRegistrationEvent event) {
        try {
            kafkaTemplate.send(USER_TOPIC, event.getEmail(), event);
            log.info("Event sent: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish user registered event: {}", e.getMessage());
        }
    }
}
