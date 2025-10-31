package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponseDto {
    private Integer slotId;
    private Integer panelistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String accessStatus;
    private String panelistName;
}
