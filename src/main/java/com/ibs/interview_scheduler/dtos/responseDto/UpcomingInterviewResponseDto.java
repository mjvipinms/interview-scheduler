package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingInterviewResponseDto {
    private String candidateName;
    private String role;
    private LocalDateTime interviewDate;
    private String mode;
}
