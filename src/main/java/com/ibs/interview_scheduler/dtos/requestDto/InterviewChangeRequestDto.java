package com.ibs.interview_scheduler.dtos.requestDto;

import com.ibs.interview_scheduler.entity.Interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewChangeRequestDto {
    private Integer interviewId;
    private Integer panelId;
    private String reason;
    private String status;
}
