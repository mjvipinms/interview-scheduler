package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.cache.UserCacheService;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.*;
import com.ibs.interview_scheduler.entity.Interview;
import com.ibs.interview_scheduler.enums.InterviewResult;
import com.ibs.interview_scheduler.enums.InterviewStatus;
import com.ibs.interview_scheduler.events.NotificationEvent;
import com.ibs.interview_scheduler.exception.CustomException;
import com.ibs.interview_scheduler.publisher.InterviewEventPublisher;
import com.ibs.interview_scheduler.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private SlotService slotService;
    @Mock private UserCacheService userCacheService;
    @Mock private InterviewEventPublisher interviewEventPublisher;

    @InjectMocks private InterviewService interviewService;

    private Interview interview;
    private InterviewRequestDto request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
       // mockStatic(UserContext.class);
//        when(UserContext.getUserName()).thenReturn("HRUser");

        interview = Interview.builder()
                .interviewId(1)
                .candidateId(10)
                .slotId(20)
                .hrId(5)
                .interviewType("TECHNICAL")
                .result(InterviewResult.PENDING.toString())
                .interviewStatus(InterviewStatus.CONFIRMED.toString())
                .panelistIds("3,4")
                .createdAt(LocalDateTime.now())
                .build();

        request = new InterviewRequestDto();
        request.setCandidateId(10);
        request.setSlotId(20);
        request.setHrId(5);
        request.setInterviewType("TECHNICAL");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setPanelistIds(List.of(3, 4));
    }

    // -------------------- createInterview --------------------
    @Test
    void createInterview_shouldCreateSuccessfully() {
        when(interviewRepository.existsByCandidateIdAndStartTimeBetween(anyInt(), any(), any())).thenReturn(false);
        when(interviewRepository.existsByPanelistIdsContainingAndStartTimeBetween(anyString(), any(), any())).thenReturn(false);
        when(interviewRepository.save(any())).thenReturn(interview);
        when(slotService.getSlotsByPanelIdStartTimeEndTime(anyList(), any(), any())).thenReturn(
                List.of(new SlotResponseDto(20, 3, LocalDateTime.now(), LocalDateTime.now(), "UNBOOKED","",""))
        );
        doNothing().when(slotService).updateSlotStatus(anyInt(), anyString());
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());

        InterviewResponseDto result = interviewService.createInterview(request);

        assertThat(result).isNotNull();
        assertThat(result.getCandidateId()).isEqualTo(10);
        verify(interviewRepository).save(any());
        verify(slotService, atLeastOnce()).updateSlotStatus(anyInt(), eq("BOOKED"));
        verify(interviewEventPublisher, atLeastOnce()).publishInterviewCreated(any(NotificationEvent.class));
    }

    @Test
    void createInterview_shouldThrowCustomExceptionWhenCandidateBusy() {
        when(interviewRepository.existsByCandidateIdAndStartTimeBetween(anyInt(), any(), any())).thenReturn(true);

        CustomException ex = assertThrows(CustomException.class, () -> interviewService.createInterview(request));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    // -------------------- getAllInterviews --------------------
    @Test
    void getAllInterviews_shouldReturnListOfResponses() {
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(interviewRepository.findAll()).thenReturn(List.of(interview));

        List<InterviewResponseDto> result = interviewService.getAllInterviews();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCandidateId()).isEqualTo(10);
    }

    // -------------------- getInterviewById --------------------
    @Test
    void getInterviewById_shouldReturnInterviewResponse() {
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));

        InterviewResponseDto result = interviewService.getInterviewById(1);

        assertThat(result.getInterviewId()).isEqualTo(1);
        assertThat(result.getCandidateId()).isEqualTo(10);
    }

    // -------------------- updateInterview --------------------
    @Test
    void updateInterview_shouldUpdateResultBasedOnRating() {
        Interview existing = Interview.builder().interviewId(1)
                .panelistIds("3,4").build();
        request.setRating(5);
        request.setFeedback("Excellent");
        request.setPanelistIds(List.of(3,4));

        when(interviewRepository.findById(1)).thenReturn(Optional.of(existing));
        when(interviewRepository.save(any())).thenReturn(existing);

        InterviewResponseDto result = interviewService.updateInterview(1, request);

        assertThat(result).isNotNull();
        verify(interviewEventPublisher).publishInterviewCreated(any(NotificationEvent.class));
        verify(interviewRepository).save(any());
    }

    @Test
    void updateInterview_shouldThrowExceptionWhenNotFound() {
        when(interviewRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> interviewService.updateInterview(999, request));
    }

    // -------------------- deleteInterview --------------------
    @Test
    void deleteInterview_shouldMarkAsDeletedAndUnbookSlot() {
        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));

        interviewService.deleteInterview(1);

        verify(interviewRepository).save(any(Interview.class));
        verify(slotService).updateSlotStatus(20, "UNBOOKED");
    }

    // -------------------- getInterviewSummary --------------------
    @Test
    void getInterviewSummary_shouldReturnSummary() {
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(interviewRepository.countAssignedInterviewsThisMonth(anyString())).thenReturn(5);
        Interview i = new Interview();
        i.setCandidateId(10);
        i.setInterviewType("TECH");
        i.setStartTime(LocalDateTime.now());
        i.setMode("ONLINE");
        when(interviewRepository.findUpcomingInterviewsForWeek(anyString(), any(), any())).thenReturn(List.of(i));

        InterviewSummaryResponseDto result = interviewService.getInterviewSummary("3");

        assertThat(result.getTotalAssignedThisMonth()).isEqualTo(5);
        assertThat(result.getUpcomingInterviews()).hasSize(1);
    }

    // -------------------- getInterviewsByPanelId --------------------
    @Test
    void getInterviewsByPanelId_shouldReturnInterviews() {
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(interviewRepository.findConfirmedInterviewsByPanelId(anyString())).thenReturn(List.of(interview));

        List<InterviewResponseDto> result = interviewService.getInterviewsByPanelId(3);

        assertThat(result).hasSize(1);
        verify(interviewRepository).findConfirmedInterviewsByPanelId("3");
    }

    // -------------------- getInterviewsByCandidateId --------------------
    @Test
    void getInterviewsByCandidateId_shouldReturnInterviews() {
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(interviewRepository.findInterviewsByCandidateId(10)).thenReturn(List.of(interview));

        List<InterviewResponseDto> result = interviewService.getInterviewsByCandidateId(10);

        assertThat(result).hasSize(1);
        verify(interviewRepository).findInterviewsByCandidateId(10);
    }

    // -------------------- rescheduleInterview --------------------
    @Test
    void rescheduleInterview_shouldUpdateSlotAndSave() {
        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));
        when(slotService.getSlotsByPanelIdStartTimeEndTime(anyList(), any(), any()))
                .thenReturn(List.of(new SlotResponseDto(30, 3, LocalDateTime.now(), LocalDateTime.now(), "UNBOOKED","","")));
        when(interviewRepository.save(any())).thenReturn(interview);
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());

        request.setSlotId(99);
        request.setPanelistIds(List.of(3));
        InterviewResponseDto result = interviewService.rescheduleInterview(1, request);

        assertThat(result).isNotNull();
        verify(slotService, atLeastOnce()).updateSlotStatus(anyInt(), anyString());
        verify(interviewRepository).save(any(Interview.class));
    }

    // -------------------- helpers --------------------
    private List<UserResponseDTO> mockUsers() {
        return List.of(
                new UserResponseDTO(10, "cand", "999", "pwd", "cand@mail", "Candidate", true, 4, "CANDIDATE", null, null),
                new UserResponseDTO(3, "panel", "888", "pwd", "panel@mail", "Panel", true, 3, "PANEL", null, null),
                new UserResponseDTO(5, "hr", "777", "pwd", "hr@mail", "HR", true, 2, "HR", null, null)
        );
    }
}
