package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.dtos.responseDto.*;
import com.ibs.interview_scheduler.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHrDashboardSummary_shouldReturnSummaryWhenRoleIsHR() {
        HrDashboardResponseDto dto = new HrDashboardResponseDto();
        dto.setTotalCandidates(10);
        dto.setScheduledInterviews(4);
        dto.setSelected(5);
        dto.setRejected(1);

        when(dashboardService.getHrDashboardSummary()).thenReturn(dto);

        ResponseEntity<HrDashboardResponseDto> response = dashboardController.getHrDashboardSummary("HR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(dashboardService).getHrDashboardSummary();
    }

    @Test
    void getHrDashboardSummary_shouldReturnForbiddenWhenRoleIsNotHR() {
        ResponseEntity<HrDashboardResponseDto> response = dashboardController.getHrDashboardSummary("ADMIN");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(dashboardService, never()).getHrDashboardSummary();
    }

    @Test
    void getPanelDashboard_shouldReturnSummaryWhenRoleIsPanel() {
        PanelDashboardResponseDto dto = new PanelDashboardResponseDto();
        SlotSummaryResponseDto sDto = new SlotSummaryResponseDto(10,5,2);
        InterviewSummaryResponseDto iDto = new InterviewSummaryResponseDto(10, List.of( new UpcomingInterviewResponseDto("Test","CANDIDATE", LocalDateTime.now(),"Online")));
        dto.setSlotSummaryResponseDto(sDto);
        dto.setInterviewSummaryResponseDto(iDto);

        when(dashboardService.getPanelDashboard(5)).thenReturn(dto);

        ResponseEntity<PanelDashboardResponseDto> response = dashboardController.getPanelDashboard(5, "PANEL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(dashboardService).getPanelDashboard(5);
    }

    @Test
    void getPanelDashboard_shouldReturnForbiddenWhenRoleIsNotPanel() {
        ResponseEntity<PanelDashboardResponseDto> response = dashboardController.getPanelDashboard(5, "HR");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(dashboardService, never()).getPanelDashboard(anyInt());
    }

    @Test
    void getAdminDashboardSummary_shouldReturnSummaryWhenRoleIsAdmin() {
        AdminDashboardResponseDto dto = new AdminDashboardResponseDto();
        AdminDashboardResponseDto.CountDto cdto = new AdminDashboardResponseDto.CountDto();
        cdto.setInactiveCount(2);
        cdto.setActiveCount(10);

        when(dashboardService.getAdminDashboardSummary()).thenReturn(dto);

        ResponseEntity<AdminDashboardResponseDto> response = dashboardController.getAdminDashboardSummary("ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(dashboardService).getAdminDashboardSummary();
    }

    @Test
    void getAdminDashboardSummary_shouldReturnForbiddenWhenRoleIsNotAdmin() {
        ResponseEntity<AdminDashboardResponseDto> response = dashboardController.getAdminDashboardSummary("PANEL");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(dashboardService, never()).getAdminDashboardSummary();
    }
}
