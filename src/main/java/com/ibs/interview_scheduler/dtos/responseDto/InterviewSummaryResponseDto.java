package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewSummaryResponseDto {
    private int totalAssignedThisMonth;
    private List<UpcomingInterviewResponseDto> upcomingInterviews;
}
