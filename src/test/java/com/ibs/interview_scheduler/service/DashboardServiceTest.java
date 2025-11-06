package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.dtos.responseDto.*;
import com.ibs.interview_scheduler.enums.InterviewResult;
import com.ibs.interview_scheduler.feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Mock
    private InterviewService interviewService;

    @Mock
    private UserClient userClient;

    @Mock
    private SlotService slotService;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------- HR DASHBOARD ---------------------
    @Test
    void getHrDashboardSummary_shouldReturnValidSummary() {
        // Prepare mock data
        UserResponseDTO candidate = new UserResponseDTO(1, "cand", "999", "pwd", "c@mail", "Candidate 1", true, 4, "CANDIDATE", null, null);
        UserResponseDTO panel = new UserResponseDTO(2, "panel", "888", "pwd", "p@mail", "Panel 1", true, 3, "PANEL", null, null);
        List<UserResponseDTO> users = List.of(candidate, panel);

        InterviewResponseDto interview = new InterviewResponseDto();
        interview.setCandidateId(1);
        interview.setInterviewStatus("CONFIRMED");
        interview.setStartTime(LocalDateTime.now().plusDays(1));
        interview.setResult(InterviewResult.SELECTED.toString());
        interview.setInterviewType("TECHNICAL");
        interview.setMode("ONLINE");
        interview.setIsDeleted(false);

        SlotResponseDto slot = new SlotResponseDto();
        slot.setPanelistId(2);
        slot.setStatus("UNBOOKED");

        when(userClient.getAllUsers()).thenReturn(users);
        when(interviewService.getAllInterviews()).thenReturn(List.of(interview));
        when(slotService.getAllSlots()).thenReturn(List.of(slot));

        // Execute
        HrDashboardResponseDto result = dashboardService.getHrDashboardSummary();

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getTotalCandidates()).isEqualTo(1);
        assertThat(result.getAssigned()).isEqualTo(1);
        assertThat(result.getPending()).isZero();
        assertThat(result.getAvailableSlots()).isEqualTo(1);
        assertThat(result.getScheduledInterviews()).isEqualTo(1);
        assertThat(result.getSelected()).isEqualTo(1);
        assertThat(result.getRejected()).isZero();
        assertThat(result.getUpcoming()).hasSize(1);

        verify(userClient, times(1)).getAllUsers();
        verify(interviewService, times(1)).getAllInterviews();
        verify(slotService, times(1)).getAllSlots();
    }

    @Test
    void getHrDashboardSummary_shouldThrowExceptionWhenServiceFails() {
        when(userClient.getAllUsers()).thenThrow(new RuntimeException("Feign error"));

        assertThrows(RuntimeException.class, () -> dashboardService.getHrDashboardSummary());
    }

    // --------------------- PANEL DASHBOARD ---------------------
    @Test
    void getPanelDashboard_shouldReturnCombinedDashboard() {
        SlotSummaryResponseDto slotSummary = new SlotSummaryResponseDto();
        slotSummary.setAppliedSlots(5);

        InterviewSummaryResponseDto interviewSummary = new InterviewSummaryResponseDto();
        interviewSummary.setTotalAssignedThisMonth(3);

        when(slotService.getSlotSummary(2)).thenReturn(slotSummary);
        when(interviewService.getInterviewSummary("2")).thenReturn(interviewSummary);

        PanelDashboardResponseDto response = dashboardService.getPanelDashboard(2);

        assertThat(response).isNotNull();
        assertThat(response.getSlotSummaryResponseDto().getAppliedSlots()).isEqualTo(5);
        assertThat(response.getInterviewSummaryResponseDto().getTotalAssignedThisMonth()).isEqualTo(3);

        verify(slotService, times(1)).getSlotSummary(2);
        verify(interviewService, times(1)).getInterviewSummary("2");
    }

    @Test
    void getPanelDashboard_shouldThrowExceptionWhenDependentServiceFails() {
        when(slotService.getSlotSummary(1)).thenThrow(new RuntimeException("Slot error"));

        assertThrows(RuntimeException.class, () -> dashboardService.getPanelDashboard(1));
    }

    // --------------------- ADMIN DASHBOARD ---------------------
    @Test
    void getAdminDashboardSummary_shouldReturnValidCounts() {
        UserResponseDTO hrActive = new UserResponseDTO(1, "hr1", "111", "pwd", "hr@mail", "HR User", true, 2, "HR", null, null);
        UserResponseDTO hrInactive = new UserResponseDTO(2, "hr2", "222", "pwd", "hr2@mail", "HR User 2", false, 2, "HR", null, null);
        UserResponseDTO panelActive = new UserResponseDTO(3, "p1", "333", "pwd", "p1@mail", "Panel 1", true, 3, "PANEL", null, null);
        UserResponseDTO candidateActive = new UserResponseDTO(4, "c1", "444", "pwd", "c1@mail", "Candidate", true, 4, "CANDIDATE", null, null);
        UserResponseDTO candidateInactive = new UserResponseDTO(5, "c2", "555", "pwd", "c2@mail", "Candidate 2", false, 4, "CANDIDATE", null, null);

        when(userClient.getAllUsers()).thenReturn(List.of(hrActive, hrInactive, panelActive, candidateActive, candidateInactive));

        AdminDashboardResponseDto result = dashboardService.getAdminDashboardSummary();

        assertThat(result).isNotNull();
        assertThat(result.getHrUsers().getActiveCount()).isEqualTo(1);
        assertThat(result.getHrUsers().getInactiveCount()).isEqualTo(1);
        assertThat(result.getPanelists().getActiveCount()).isEqualTo(1);
        assertThat(result.getCandidates().getActiveCount()).isEqualTo(1);
        assertThat(result.getCandidates().getInactiveCount()).isEqualTo(1);

        verify(userClient, times(1)).getAllUsers();
    }

    @Test
    void getAdminDashboardSummary_shouldThrowExceptionOnFailure() {
        when(userClient.getAllUsers()).thenThrow(new RuntimeException("User service unavailable"));

        assertThrows(RuntimeException.class, () -> dashboardService.getAdminDashboardSummary());
    }
}
