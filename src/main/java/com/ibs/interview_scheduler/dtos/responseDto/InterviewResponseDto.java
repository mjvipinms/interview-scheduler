package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewResponseDto {
    private Integer interviewId;
    private Integer candidateId;
    private Integer slotId;
    private Integer hrId;
    private List<String> panellistIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String interviewType;
    private String interviewStatus;
    private String mode;
    private String result;
    private String feedback;
    private LocalDateTime createdAt;
    private String accessStatus;
    private String candidateName;
    private List<String> panellistNames;
    private Boolean isDeleted;
}
