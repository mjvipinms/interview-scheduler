package com.ibs.interview_scheduler.publisher;

import com.ibs.interview_scheduler.events.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InterviewEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InterviewEventPublisher interviewEventPublisher;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        event = NotificationEvent.builder()
                .eventType("INTERVIEWCREATED")
                .payload("test-payload")
                .build();
    }

    @Test
    void publishInterviewCreated_shouldSendEventSuccessfully() {
        // Act
        interviewEventPublisher.publishInterviewCreated(event);

        // Assert
        verify(kafkaTemplate, times(1)).send("interview-events", event);
    }

    @Test
    void publishInterviewCreated_shouldThrowRuntimeExceptionOnKafkaError() {
        doThrow(new RuntimeException("Kafka down"))
                .when(kafkaTemplate).send("interview-events", event);

        assertThatThrownBy(() -> interviewEventPublisher.publishInterviewCreated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka down");

        verify(kafkaTemplate, times(1)).send("interview-events", event);
    }
}
