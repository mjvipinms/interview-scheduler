package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewChangeRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewChangeRequestResponseDto;
import com.ibs.interview_scheduler.service.InterviewChangeRequestService;
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

class InterviewChangeRequestControllerTest {

    @Mock
    private InterviewChangeRequestService service;

    @InjectMocks
    private InterviewChangeRequestController controller;

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

    // ----------------------------- CREATE CHANGE REQUEST -----------------------------
    @Test
    void createChangeRequest_shouldReturnOkWhenAuthorized() {
        InterviewChangeRequestDto request = new InterviewChangeRequestDto();
        InterviewChangeRequestResponseDto responseDto = new InterviewChangeRequestResponseDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("PANEL"))).thenReturn(true);

        when(service.createChangeRequest(request)).thenReturn(responseDto);

        ResponseEntity<?> response = controller.createChangeRequest(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(service).createChangeRequest(request);
    }

    @Test
    void createChangeRequest_shouldReturnForbiddenWhenUnauthorized() {
        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("PANEL"))).thenReturn(false);

        ResponseEntity<?> response = controller.createChangeRequest(new InterviewChangeRequestDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody());
        verify(service, never()).createChangeRequest(any());
    }

    // ----------------------------- GET PENDING REQUESTS (HR only) -----------------------------
    @Test
    void getPendingRequests_shouldReturnOkWhenAuthorized() {
        List<InterviewChangeRequestResponseDto> list = List.of(new InterviewChangeRequestResponseDto());

        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("HR"))).thenReturn(true);
        when(service.getPendingRequests()).thenReturn(list);

        ResponseEntity<?> response = controller.getPendingRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
        verify(service).getPendingRequests();
    }

    @Test
    void getPendingRequests_shouldReturnForbiddenWhenUnauthorized() {
        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("HR"))).thenReturn(false);

        ResponseEntity<?> response = controller.getPendingRequests();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody());
        verify(service, never()).getPendingRequests();
    }

    // ----------------------------- GET PENDING BY PANEL -----------------------------
    @Test
    void getPendingRequestsByPanelId_shouldReturnOkWhenAuthorized() {
        List<InterviewChangeRequestResponseDto> list = List.of(new InterviewChangeRequestResponseDto());

        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("PANEL"))).thenReturn(true);
        when(service.getPendingRequestsByPanelId(5)).thenReturn(list);

        ResponseEntity<?> response = controller.getPendingRequestsByPanelId(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
        verify(service).getPendingRequestsByPanelId(5);
    }

    @Test
    void getPendingRequestsByPanelId_shouldReturnForbiddenWhenUnauthorized() {
        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("PANEL"))).thenReturn(false);

        ResponseEntity<?> response = controller.getPendingRequestsByPanelId(10);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody());
        verify(service, never()).getPendingRequestsByPanelId(any());
    }

    // ----------------------------- UPDATE REQUEST (HR only) -----------------------------
    @Test
    void updateChangeRequest_shouldReturnOkWhenAuthorized() {
        InterviewChangeRequestDto request = new InterviewChangeRequestDto();
        InterviewChangeRequestResponseDto updated = new InterviewChangeRequestResponseDto();

        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("HR"))).thenReturn(true);
        when(service.updateChangeRequest(1, request)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateChangeRequest(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        verify(service).updateChangeRequest(1, request);
    }

    @Test
    void updateChangeRequest_shouldReturnForbiddenWhenUnauthorized() {
        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("HR"))).thenReturn(false);

        ResponseEntity<?> response = controller.updateChangeRequest(1, new InterviewChangeRequestDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody());
        verify(service, never()).updateChangeRequest(anyInt(), any());
    }

    // ----------------------------- BULK APPROVE (HR only) -----------------------------
    @Test
    void bulkApprove_shouldReturnOkWhenAuthorized() {
        List<Integer> ids = List.of(1, 2, 3);
        List<InterviewChangeRequestResponseDto> updatedList = List.of(new InterviewChangeRequestResponseDto());

        userContextMock.when(UserContext::getUserRole).thenReturn("HR");
        roleValidatorMock.when(() -> isAuthorized("HR", List.of("HR"))).thenReturn(true);
        when(service.bulkApproveRequests(ids)).thenReturn(updatedList);

        ResponseEntity<?> response = controller.bulkApprove(ids);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedList, response.getBody());
        verify(service).bulkApproveRequests(ids);
    }

    @Test
    void bulkApprove_shouldReturnForbiddenWhenUnauthorized() {
        userContextMock.when(UserContext::getUserRole).thenReturn("PANEL");
        roleValidatorMock.when(() -> isAuthorized("PANEL", List.of("HR"))).thenReturn(false);

        ResponseEntity<?> response = controller.bulkApprove(List.of(1, 2));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody());
        verify(service, never()).bulkApproveRequests(any());
    }
}
