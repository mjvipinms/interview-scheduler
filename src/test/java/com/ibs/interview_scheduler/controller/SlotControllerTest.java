package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.dtos.requestDto.SlotRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SlotControllerTest {

    @Mock
    private SlotService slotService;

    @InjectMocks
    private SlotController slotController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------- CREATE SLOT ----------------------
    @Test
    void createSlot_shouldReturnCreatedSlot() {
        SlotRequestDto request = new SlotRequestDto();
        SlotResponseDto expectedResponse = new SlotResponseDto();

        when(slotService.createSlot(request)).thenReturn(expectedResponse);

        ResponseEntity<SlotResponseDto> response = slotController.createSlot(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(slotService).createSlot(request);
    }

    // ---------------------- GET ALL SLOTS ----------------------
    @Test
    void getAllSlots_shouldReturnListOfSlots() {
        List<SlotResponseDto> expectedList = List.of(new SlotResponseDto(), new SlotResponseDto());
        when(slotService.getAllSlots()).thenReturn(expectedList);

        ResponseEntity<List<SlotResponseDto>> response = slotController.getAllSlots();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
        verify(slotService).getAllSlots();
    }

    // ---------------------- GET SLOT BY ID ----------------------
    @Test
    void getSlot_shouldReturnSlotById() {
        SlotResponseDto expectedResponse = new SlotResponseDto();
        when(slotService.getSlotById(1)).thenReturn(expectedResponse);

        ResponseEntity<SlotResponseDto> response = slotController.getSlot(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(slotService).getSlotById(1);
    }

    // ---------------------- UPDATE SLOT ----------------------
    @Test
    void updateSlot_shouldReturnUpdatedSlot() {
        SlotRequestDto request = new SlotRequestDto();
        SlotResponseDto updatedSlot = new SlotResponseDto();

        when(slotService.updateSlot(1, request)).thenReturn(updatedSlot);

        ResponseEntity<SlotResponseDto> response = slotController.updateSlot(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSlot, response.getBody());
        verify(slotService).updateSlot(1, request);
    }

    // ---------------------- DELETE SLOT ----------------------
    @Test
    void deleteSlot_shouldCallServiceOnce() {
        doNothing().when(slotService).deleteSlot(1);

        slotController.deleteSlot(1);

        verify(slotService, times(1)).deleteSlot(1);
    }

    // ---------------------- GET OVERLAPPING SLOTS ----------------------
    @Test
    void getAvailablePanelists_shouldReturnOverlappingSlots() {
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 4, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 4, 12, 0);

        List<SlotResponseDto> overlappingSlots = List.of(new SlotResponseDto());
        when(slotService.getOverLappingSlot(startTime, endTime)).thenReturn(overlappingSlots);

        ResponseEntity<List<SlotResponseDto>> response = slotController.getAvailablePanelists(startTime, endTime);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(overlappingSlots, response.getBody());
        verify(slotService).getOverLappingSlot(startTime, endTime);
    }

    // ---------------------- GET ALL SLOTS BY PANEL ID ----------------------
    @Test
    void getAllSlotsByPanelID_shouldReturnListOfSlots() {
        List<SlotResponseDto> expectedSlots = List.of(new SlotResponseDto());
        when(slotService.getAllSlotsByPanelID(5)).thenReturn(expectedSlots);

        ResponseEntity<List<SlotResponseDto>> response = slotController.getAllSlotsByPanelID(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSlots, response.getBody());
        verify(slotService).getAllSlotsByPanelID(5);
    }

    // ---------------------- GET ALL AVAILABLE SLOTS ----------------------
    @Test
    void getAllAvailableSlots_shouldReturnAvailableSlots() {
        List<SlotResponseDto> availableSlots = List.of(new SlotResponseDto(), new SlotResponseDto());
        when(slotService.getAllAvailableSlots()).thenReturn(availableSlots);

        ResponseEntity<List<SlotResponseDto>> response = slotController.getAllAvailableSlots();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(availableSlots, response.getBody());
        verify(slotService).getAllAvailableSlots();
    }
}
