package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewResponseDto;
import com.ibs.interview_scheduler.service.InterviewService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.ibs.interview_scheduler.utils.RoleValidator.isAuthorized;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private MockedStatic<UserContext> userContextMock;
    private MockedStatic<com.ibs.interview_scheduler.utils.RoleValidator> roleValidatorMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userContextMock = mockStatic(UserContext.class);
        roleValidatorMock = mockStatic(com.ibs.interview_scheduler.utils.RoleValidator.class);
    }

    @AfterEach
    void tearDown() {
        userContextMock.close();
        roleValidatorMock.close();
    }

    // ---------------- CREATE INTERVIEW ----------------
    @Test
    void createInterview_shouldReturnOkWhenAuthorized() {
        InterviewRequestDto request = new InterviewRequestDto();
        InterviewResponseDto expectedResponse = new InterviewResponseDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("HR"))).thenReturn(true);
        when(interviewService.createInterview(request)).thenReturn(expectedResponse);

        ResponseEntity<InterviewResponseDto> response = interviewController.createInterview(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(interviewService).createInterview(request);
    }

    @Test
    void createInterview_shouldReturnForbiddenWhenUnauthorized() {
        InterviewRequestDto request = new InterviewRequestDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("HR"))).thenReturn(false);

        ResponseEntity<InterviewResponseDto> response = interviewController.createInterview(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("User permission denied", response.getBody().getAccessStatus());
        verify(interviewService, never()).createInterview(any());
    }

    // ---------------- GET ALL INTERVIEWS ----------------
    @Test
    void getAllInterviews_shouldReturnList() {
        List<InterviewResponseDto> expected = List.of(new InterviewResponseDto());
        when(interviewService.getAllInterviews()).thenReturn(expected);

        ResponseEntity<List<InterviewResponseDto>> response = interviewController.getAllInterviews();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(interviewService).getAllInterviews();
    }

    // ---------------- GET INTERVIEW BY ID ----------------
    @Test
    void getInterview_shouldReturnInterview() {
        InterviewResponseDto dto = new InterviewResponseDto();
        when(interviewService.getInterviewById(1)).thenReturn(dto);

        ResponseEntity<InterviewResponseDto> response = interviewController.getInterview(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(interviewService).getInterviewById(1);
    }

    // ---------------- UPDATE INTERVIEW ----------------
    @Test
    void updateInterview_shouldReturnOkWhenAuthorized() {
        InterviewRequestDto request = new InterviewRequestDto();
        InterviewResponseDto updated = new InterviewResponseDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("HR", "PANEL"))).thenReturn(true);
        when(interviewService.updateInterview(1, request)).thenReturn(updated);

        ResponseEntity<InterviewResponseDto> response = interviewController.updateInterview(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        verify(interviewService).updateInterview(1, request);
    }

    @Test
    void updateInterview_shouldReturnForbiddenWhenUnauthorized() {
        InterviewRequestDto request = new InterviewRequestDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("CANDIDATE");
        roleValidatorMock.when(() -> isAuthorized("CANDIDATE", List.of("HR", "PANEL"))).thenReturn(false);

        ResponseEntity<InterviewResponseDto> response = interviewController.updateInterview(1, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("User permission denied", response.getBody().getAccessStatus());
        verify(interviewService, never()).updateInterview(anyInt(), any());
    }

    // ---------------- DELETE INTERVIEW ----------------
    @Test
    void deleteInterview_shouldCallService() {
        doNothing().when(interviewService).deleteInterview(1);

        interviewController.deleteInterview(1);

        verify(interviewService).deleteInterview(1);
    }

// -
}