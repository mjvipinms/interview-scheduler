package com.ibs.interview_scheduler.publisher;

import com.ibs.interview_scheduler.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInterviewCreated(NotificationEvent event) {
        try {
            log.info("Sending event{}",event);
            kafkaTemplate.send("interview-events", event);
        } catch (Exception e) {
            log.error("Exception occurred while sending interview created event, {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
