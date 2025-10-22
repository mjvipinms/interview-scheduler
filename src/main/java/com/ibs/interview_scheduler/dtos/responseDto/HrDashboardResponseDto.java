package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrDashboardResponseDto {
    private long totalCandidates;
    private long assigned;
    private long pending;
    private long scheduledInterviews;
    private long availableSlots;
    private long pendingPanelists;
    private List<UpcomingInterviewResponseDto> upcoming;
    private long selected;
    private long rejected;
}
