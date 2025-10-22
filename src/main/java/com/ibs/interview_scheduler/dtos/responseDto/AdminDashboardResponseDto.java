package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponseDto {
    private CountDto hrUsers;
    private CountDto panelists;
    private CountDto candidates;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountDto {
        private long activeCount;
        private long inactiveCount;
    }
}
