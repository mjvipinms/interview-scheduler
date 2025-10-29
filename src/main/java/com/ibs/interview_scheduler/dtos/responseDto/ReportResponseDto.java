package com.ibs.interview_scheduler.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private List<Map<String, Object>> data;
    private int page;
    private int size;
    private long total;
}
