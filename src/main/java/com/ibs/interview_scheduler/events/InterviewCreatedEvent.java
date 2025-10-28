package com.ibs.interview_scheduler.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InterviewCreatedEvent extends BaseEvent{
    private Integer interviewId;
    private String candidateEmail;
    private String panelEmail;
    private String hrEmail;
    private LocalDateTime startTime;
    private String createdBy;
}
