package com.ibs.interview_scheduler.dtos.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SlotRequestDto {
    private Integer panelistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
