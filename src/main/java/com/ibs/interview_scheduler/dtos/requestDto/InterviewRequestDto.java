package com.ibs.interview_scheduler.dtos.requestDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewRequestDto {
    private Integer candidateId;
    private Integer slotId;
    private Integer hrId;
    private List<Integer> panelistIds;
    private String mode;
    private String interviewType;
    private String feedback;
    private String result;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer rating;

}
