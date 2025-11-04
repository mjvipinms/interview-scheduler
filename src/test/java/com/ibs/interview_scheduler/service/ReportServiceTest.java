package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.cache.UserCacheService;
import com.ibs.interview_scheduler.dtos.responseDto.ReportResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import com.ibs.interview_scheduler.feign.UserClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock private EntityManager entityManager;
    @Mock private UserClient userClient;
    @Mock private UserCacheService userCacheService;
    @Mock private Query mockQuery;

    @InjectMocks private ReportService reportService;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        start = LocalDateTime.now().minusDays(10);
        end = LocalDateTime.now();
    }

    // ------------------------ getReport ------------------------

    @Test
    void getReport_shouldCallInterviewReport() {
        mockInterviewQuery();

        when(userCacheService.getAllUsers()).thenReturn(mockUsers());
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockInterviewRows());
        when(entityManager.createNativeQuery("SELECT COUNT(*) FROM interviews ")).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(1L);

        ReportResponseDto result = reportService.getReport("interview", start, end, 1, 10, "created_at", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getData()).hasSize(1);
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
    }

    @Test
    void getReport_shouldCallSlotReport() {
        mockSlotQuery();

        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockSlotRows());
        when(entityManager.createNativeQuery("SELECT COUNT(*) FROM slots WHERE is_deleted = false"))
                .thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(2L);

        ReportResponseDto result = reportService.getReport("slots", start, end, 1, 10, "created_at", "desc");

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getData()).hasSize(1);
        verify(mockQuery, atLeastOnce()).getResultList();
    }

    @Test
    void getReport_shouldCallCandidateReport() {
        ReportResponseDto expected = new ReportResponseDto(List.of(Map.of("user", "John")), 1, 10, 5L);
        when(userClient.getUserReport(eq("CANDIDATE"), any(), any(), eq(1), eq(10), any(), any()))
                .thenReturn(expected);

        ReportResponseDto result = reportService.getReport("candidate", start, end, 1, 10, "email", "asc");

        assertThat(result.getData()).hasSize(1);
        verify(userClient).getUserReport(eq("CANDIDATE"), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    void getReport_shouldCallPanelReport() {
        ReportResponseDto expected = new ReportResponseDto(List.of(Map.of("user", "Panel1")), 1, 10, 5L);
        when(userClient.getUserReport(eq("PANEL"), any(), any(), eq(1), eq(10), any(), any()))
                .thenReturn(expected);

        ReportResponseDto result = reportService.getReport("panels", start, end, 1, 10, "email", "asc");

        assertThat(result.getData()).hasSize(1);
        verify(userClient).getUserReport(eq("PANEL"), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    void getReport_shouldThrowExceptionForInvalidType() {
        assertThrows(IllegalArgumentException.class, () ->
                reportService.getReport("invalid-type", start, end, 1, 10, "id", "asc"));
    }

    // ------------------------ Exception Handling ------------------------

    @Test
    void getInterviewReport_shouldThrowRuntimeExceptionOnError() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("DB Error"));
        when(userCacheService.getAllUsers()).thenReturn(mockUsers());

        assertThrows(RuntimeException.class, () ->
                reportService.getReport("interview", start, end, 1, 10, "created_at", "asc"));
    }

    @Test
    void getSlotReport_shouldThrowRuntimeExceptionOnError() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("Slot DB error"));

        assertThrows(RuntimeException.class, () ->
                reportService.getReport("slots", start, end, 1, 10, "created_at", "asc"));
    }

    // ------------------------ Helpers ------------------------

    private void mockInterviewQuery() {
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setFirstResult(anyInt())).thenReturn(mockQuery);
        when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
    }

    private void mockSlotQuery() {
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.setFirstResult(anyInt())).thenReturn(mockQuery);
        when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
    }

    private List<UserResponseDTO> mockUsers() {
        return List.of(
                new UserResponseDTO(1, "cand", "999", "pwd", "cand@mail", "Candidate", true, 4, "CANDIDATE", null, null),
                new UserResponseDTO(2, "hr", "888", "pwd", "hr@mail", "HR", true, 2, "HR", null, null),
                new UserResponseDTO(3, "panel", "777", "pwd", "panel@mail", "Panel", true, 3, "PANEL", null, null)
        );
    }

    private List<Object[]> mockInterviewRows() {
        return List.<Object[]>of(new Object[]{
                100, 1, 2, "3", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                "CONFIRMED", LocalDateTime.now()
        });
    }

    private List<Object[]> mockSlotRows() {
        return List.<Object[]>of(new Object[]{
                1, 3, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "UNBOOKED", LocalDateTime.now()
        });
    }
}
