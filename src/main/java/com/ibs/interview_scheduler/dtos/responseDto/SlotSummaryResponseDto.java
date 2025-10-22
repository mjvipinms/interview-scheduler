package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotSummaryResponseDto {
    private int totalSlotsThisMonth;
    private int appliedSlots;
    private int weeklyPlanSlots;
}
