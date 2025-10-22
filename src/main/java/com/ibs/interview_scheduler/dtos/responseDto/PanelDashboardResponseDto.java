package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PanelDashboardResponseDto {

    private SlotSummaryResponseDto slotSummaryResponseDto;
    private InterviewSummaryResponseDto interviewSummaryResponseDto;
}
