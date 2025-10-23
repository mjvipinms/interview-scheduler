package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewChangeRequestResponseDto {
    private Integer interviewChangeRequestId;
    private InterviewResponseDto interviewResponseDto;
    private Integer panelId;
    private String reason;
    private String status;
}
