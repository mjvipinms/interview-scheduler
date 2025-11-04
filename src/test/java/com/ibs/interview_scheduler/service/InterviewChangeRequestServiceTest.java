package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewChangeRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewChangeRequestResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewResponseDto;
import com.ibs.interview_scheduler.entity.InterviewChangeRequest;
import com.ibs.interview_scheduler.enums.RequestStatus;
import com.ibs.interview_scheduler.repository.InterviewChangeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InterviewChangeRequestServiceTest {

    @Mock
    private InterviewService interviewService;

    @Mock
    private InterviewChangeRequestRepository changeRequestRepository;

    @Mock
    private SlotService slotService;

    @InjectMocks
    private InterviewChangeRequestService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------- CREATE ---------------------------
    @Test
    void createChangeRequest_shouldCreateNewChangeRequest() {
        InterviewChangeRequestDto dto = new InterviewChangeRequestDto();
        dto.setInterviewId(1);
        dto.setPanelId(2);
        dto.setReason("Need reschedule");

        InterviewResponseDto interview = new InterviewResponseDto();
        interview.setInterviewId(1);
        interview.setSlotId(10);

        when(interviewService.getInterviewById(1)).thenReturn(interview);

        InterviewChangeRequest savedEntity = InterviewChangeRequest.builder()
                .interviewChangeRequestId(100)
                .interviewId(1)
                .panelId(2)
                .reason("Need reschedule")
                .status("PENDING")
                .createdBy("panel_user")
                .build();

        mockStatic(UserContext.class);
        when(UserContext.getUserName()).thenReturn("panel_user");

        when(changeRequestRepository.save(any(InterviewChangeRequest.class))).thenReturn(savedEntity);

        InterviewChangeRequestResponseDto response = service.createChangeRequest(dto);

        assertThat(response).isNotNull();
        assertThat(response.getInterviewChangeRequestId()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo("PENDING");

        verify(changeRequestRepository, times(1)).save(any(InterviewChangeRequest.class));
    }

    // --------------------------- GET PENDING REQUESTS ---------------------------
    @Test
    void getPendingRequests_shouldReturnPendingRequests() {
        InterviewChangeRequest entity = InterviewChangeRequest.builder()
                .interviewChangeRequestId(101)
                .interviewId(1)
                .panelId(5)
                .status("PENDING")
                .reason("Schedule clash")
                .build();

        when(changeRequestRepository.findByStatus("PENDING")).thenReturn(List.of(entity));
        when(interviewService.getInterviewById(1)).thenReturn(new InterviewResponseDto());

        List<InterviewChangeRequestResponseDto> result = service.getPendingRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPanelId()).isEqualTo(5);
        verify(changeRequestRepository, times(1)).findByStatus("PENDING");
    }

    // --------------------------- GET PENDING BY PANEL ---------------------------
    @Test
    void getPendingRequestsByPanelId_shouldReturnRequestsForPanel() {
        InterviewChangeRequest entity = InterviewChangeRequest.builder()
                .interviewChangeRequestId(102)
                .interviewId(2)
                .panelId(9)
                .status("PENDING")
                .reason("Personal reason")
                .build();

        when(changeRequestRepository.findByPanelId(9)).thenReturn(List.of(entity));
        when(interviewService.getInterviewById(2)).thenReturn(new InterviewResponseDto());

        List<InterviewChangeRequestResponseDto> result = service.getPendingRequestsByPanelId(9);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReason()).isEqualTo("Personal reason");
        verify(changeRequestRepository).findByPanelId(9);
    }

    // --------------------------- UPDATE REQUEST ---------------------------
    @Test
    void updateChangeRequest_shouldUpdateStatusToApprovedAndDeleteSlot() {
        InterviewChangeRequest existing = InterviewChangeRequest.builder()
                .interviewChangeRequestId(103)
                .interviewId(1)
                .status("PENDING")
                .build();

        InterviewChangeRequestDto dto = new InterviewChangeRequestDto();
        dto.setInterviewId(1);
        dto.setStatus(RequestStatus.APPROVED.toString());

        InterviewResponseDto interview = new InterviewResponseDto();
        interview.setInterviewId(1);
        interview.setSlotId(20);

        when(UserContext.getUserName()).thenReturn("HR");

        when(changeRequestRepository.findById(103)).thenReturn(Optional.of(existing));
        when(interviewService.getInterviewById(1)).thenReturn(interview);
        when(changeRequestRepository.save(any())).thenReturn(existing);

        InterviewChangeRequestResponseDto response = service.updateChangeRequest(103, dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(RequestStatus.APPROVED.toString());

        verify(slotService).deleteSlot(20);
        verify(interviewService).deleteInterview(1);
        verify(changeRequestRepository).save(existing);
    }

    @Test
    void updateChangeRequest_shouldThrowExceptionWhenNotFound() {
        when(changeRequestRepository.findById(999)).thenReturn(Optional.empty());

        InterviewChangeRequestDto dto = new InterviewChangeRequestDto();
        dto.setInterviewId(1);

        assertThrows(RuntimeException.class, () -> service.updateChangeRequest(999, dto));
    }

    // --------------------------- BULK APPROVE ---------------------------
    @Test
    void bulkApproveRequests_shouldApproveAllRequests() {
        InterviewChangeRequest req1 = InterviewChangeRequest.builder()
                .interviewChangeRequestId(201)
                .interviewId(1)
                .status("PENDING").build();

        InterviewResponseDto interview = new InterviewResponseDto();
        interview.setInterviewId(1);
        interview.setSlotId(30);

        when(UserContext.getUserName()).thenReturn("HR");

        when(changeRequestRepository.findAllById(List.of(201))).thenReturn(List.of(req1));
        when(interviewService.getInterviewById(1)).thenReturn(interview);
        when(changeRequestRepository.save(any())).thenReturn(req1);

        List<InterviewChangeRequestResponseDto> result = service.bulkApproveRequests(List.of(201));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RequestStatus.APPROVED.toString());

        verify(slotService).deleteSlot(30);
        verify(interviewService).deleteInterview(1);
        verify(changeRequestRepository, times(1)).save(any());
    }

    @Test
    void bulkApproveRequests_shouldThrowRuntimeExceptionOnError() {
        when(changeRequestRepository.findAllById(any())).thenThrow(new RuntimeException("DB failure"));

        assertThrows(RuntimeException.class, () -> service.bulkApproveRequests(List.of(1)));
    }
}
