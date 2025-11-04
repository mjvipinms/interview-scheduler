package com.ibs.interview_scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InterviewSchedulerApplicationTests {

    @Test
    void contextLoads() {
        // If the context fails to start, this test will fail automatically
        assertThat(true).isTrue();
    }

    @Test
    void mainMethodRunsWithoutExceptions() {
        // This ensures that the Spring Boot main() method runs without errors
        InterviewSchedulerApplication.main(new String[]{});
        assertThat(true).isTrue();
    }
}
