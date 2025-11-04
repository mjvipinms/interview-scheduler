package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.dtos.responseDto.ReportResponseDto;
import com.ibs.interview_scheduler.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------- GET REPORT TESTS ---------------------------
    @Test
    void getReport_shouldReturnReportResponse() {
        // Given
        String type = "USER_REPORT";
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        int page = 1;
        int size = 25;
        String sortField = "userName";
        String sortDir = "asc";

        ReportResponseDto responseDto = new ReportResponseDto();
        when(reportService.getReport(type, startDate, endDate, page, size, sortField, sortDir))
                .thenReturn(responseDto);

        // When
        ResponseEntity<ReportResponseDto> response = reportController.getReport(
                type, startDate, endDate, page, size, sortField, sortDir
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
        verify(reportService).getReport(type, startDate, endDate, page, size, sortField, sortDir);
    }

    @Test
    void getReport_shouldUseDefaultPaginationAndSorting() {
        // Given
        String type = "INTERVIEW_REPORT";
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        ReportResponseDto responseDto = new ReportResponseDto();
        when(reportService.getReport(type, startDate, endDate, 1, 25, null, "asc"))
                .thenReturn(responseDto);

        // When
        ResponseEntity<ReportResponseDto> response = reportController.getReport(
                type, startDate, endDate, 1, 25, null, "asc"
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
        verify(reportService).getReport(type, startDate, endDate, 1, 25, null, "asc");
    }
}
