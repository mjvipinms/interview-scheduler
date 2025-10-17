package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewResponseDto {
    private Integer id;
    private Integer candidateId;
    private Integer slotId;
    private Integer hrId;
    private List<Integer> panellistIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String interviewType;
    private String result;
    private String feedback;
    private LocalDateTime createdAt;
    private String accessStatus;
}
