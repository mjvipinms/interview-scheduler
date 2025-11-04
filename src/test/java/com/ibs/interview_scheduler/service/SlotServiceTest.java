package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.cache.UserCacheService;
import com.ibs.interview_scheduler.dtos.requestDto.SlotRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotSummaryResponseDto;
import com.ibs.interview_scheduler.entity.Slot;
import com.ibs.interview_scheduler.enums.SlotStatus;
import com.ibs.interview_scheduler.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SlotServiceTest {

    @Mock private SlotRepository slotRepository;
    @Mock private UserCacheService userCacheService;

    @InjectMocks private SlotService slotService;

    private Slot slot;
    private SlotRequestDto request;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
       // mockStatic(UserContext.class);
        //when(UserContext.getUserName()).thenReturn("PanelUser");

        start = LocalDateTime.now().plusHours(1);
        end = start.plusHours(2);

        slot = Slot.builder()
                .slotId(1)
                .panelistId(101)
                .startTime(start)
                .endTime(end)
                .status(SlotStatus.UNBOOKED.toString())
                .isDeleted(false)
                .build();

        request = new SlotRequestDto();
        request.setPanelistId(101);
        request.setStartTime(start);
        request.setEndTime(end);
    }

    // -------------------- createSlot --------------------

    @Test
    void createSlot_shouldCreateSuccessfully() {
        when(slotRepository.existsOverlappingSlot(anyInt(), any(), any())).thenReturn(false);
        when(slotRepository.save(any())).thenReturn(slot);

        SlotResponseDto result = slotService.createSlot(request);

        assertThat(result).isNotNull();
        assertThat(result.getPanelistId()).isEqualTo(101);
        verify(slotRepository).save(any(Slot.class));
    }

    @Test
    void createSlot_shouldThrowCustomExceptionForInvalidTime() {
        request.setEndTime(request.getStartTime());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.createSlot(request));
        assertThat(ex.getMessage()).contains("Slot end time must be after start time");
    }

    @Test
    void createSlot_shouldThrowCustomExceptionForOverlap() {
        when(slotRepository.existsOverlappingSlot(anyInt(), any(), any())).thenReturn(true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> slotService.createSlot(request));
        assertThat(ex.getMessage()).contains("Slot overlaps");
    }

    // -------------------- getAllSlots --------------------

    @Test
    void getAllSlots_shouldReturnAllSlots() {
        when(userCacheService.getUserIdNameMap()).thenReturn(Map.of(101, "John Panel"));
        when(slotRepository.findAll()).thenReturn(List.of(slot));

        List<SlotResponseDto> result = slotService.getAllSlots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPanelistName()).isEqualTo("John Panel");
    }

    // -------------------- getSlotById --------------------

    @Test
    void getSlotById_shouldReturnSlot() {
        when(slotRepository.findById(1)).thenReturn(Optional.of(slot));

        SlotResponseDto result = slotService.getSlotById(1);

        assertThat(result.getSlotId()).isEqualTo(1);
    }

    @Test
    void getSlotById_shouldThrowWhenNotFound() {
        when(slotRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> slotService.getSlotById(999));
    }

    // -------------------- updateSlot --------------------

    @Test
    void updateSlot_shouldUpdateSuccessfully() {
        when(slotRepository.findById(1)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any())).thenReturn(slot);

        SlotResponseDto result = slotService.updateSlot(1, request);

        assertThat(result.getSlotId()).isEqualTo(1);
        verify(slotRepository).save(any(Slot.class));
    }

    @Test
    void updateSlot_shouldThrowWhenNotFound() {
        when(slotRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> slotService.updateSlot(1, request));
    }

    // -------------------- deleteSlot --------------------

    @Test
    void deleteSlot_shouldMarkDeleted() {
        when(slotRepository.findById(1)).thenReturn(Optional.of(slot));

        slotService.deleteSlot(1);

        verify(slotRepository).save(any(Slot.class));
        assertThat(slot.getIsDeleted()).isTrue();
    }

    // -------------------- getOverLappingSlot --------------------

    @Test
    void getOverLappingSlot_shouldReturnList() {
        when(userCacheService.getUserIdNameMap()).thenReturn(Map.of(101, "Panel"));
        when(slotRepository.findAvailablePanelistIdsInSlot(any(), any())).thenReturn(List.of(slot));

        List<SlotResponseDto> result = slotService.getOverLappingSlot(start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPanelistName()).isEqualTo("Panel");
    }

    // -------------------- getAllSlotsByPanelID --------------------

    @Test
    void getAllSlotsByPanelID_shouldReturnList() {
        when(userCacheService.getUserIdNameMap()).thenReturn(Map.of(101, "Panel"));
        when(slotRepository.findActiveSlotsByPanelistId(101)).thenReturn(List.of(slot));

        List<SlotResponseDto> result = slotService.getAllSlotsByPanelID(101);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPanelistId()).isEqualTo(101);
    }

    // -------------------- updateSlotStatus --------------------

    @Test
    void updateSlotStatus_shouldUpdateSuccessfully() {
        when(slotRepository.findById(1)).thenReturn(Optional.of(slot));

        slotService.updateSlotStatus(1, "BOOKED");

        assertThat(slot.getStatus()).isEqualTo("BOOKED");
    }

    @Test
    void updateSlotStatus_shouldThrowWhenNotFound() {
        when(slotRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> slotService.updateSlotStatus(999, "BOOKED"));
    }

    // -------------------- getSlotSummary --------------------

    @Test
    void getSlotSummary_shouldReturnSummary() {
        when(slotRepository.countTotalSlotsThisMonth(anyInt())).thenReturn(10);
        when(slotRepository.countAppliedSlotsThisMonth(anyInt())).thenReturn(3);

        SlotSummaryResponseDto result = slotService.getSlotSummary(101);

        assertThat(result.getTotalSlotsThisMonth()).isEqualTo(10);
        assertThat(result.getAppliedSlots()).isEqualTo(3);
        assertThat(result.getWeeklyPlanSlots()).isEqualTo(2);
    }

    @Test
    void getSlotSummary_shouldThrowRuntimeExceptionOnError() {
        when(slotRepository.countTotalSlotsThisMonth(anyInt())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> slotService.getSlotSummary(101));
    }

    // -------------------- getSlotsByPanelIdStartTimeEndTime --------------------

    @Test
    void getSlotsByPanelIdStartTimeEndTime_shouldReturnSlots() {
        when(userCacheService.getUserIdNameMap()).thenReturn(Map.of(101, "Panel"));
        when(slotRepository.findByPanelistIdInAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(anyList(), any(), any()))
                .thenReturn(List.of(slot));

        List<SlotResponseDto> result = slotService.getSlotsByPanelIdStartTimeEndTime(List.of(101), start, end);

        assertThat(result).hasSize(1);
        verify(slotRepository).findByPanelistIdInAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(anyList(), any(), any());
    }

    @Test
    void getSlotsByPanelIdStartTimeEndTime_shouldThrowRuntimeException() {
        when(slotRepository.findByPanelistIdInAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(anyList(), any(), any()))
                .thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () ->
                slotService.getSlotsByPanelIdStartTimeEndTime(List.of(101), start, end));
    }

    // -------------------- getAllAvailableSlots --------------------

    @Test
    void getAllAvailableSlots_shouldReturnFilteredSlots() {
        when(userCacheService.getUserIdNameMap()).thenReturn(Map.of(101, "Panel"));
        Slot slot2 = Slot.builder().slotId(2).panelistId(101).status("BOOKED").isDeleted(false).build();
        when(slotRepository.findAll()).thenReturn(List.of(slot, slot2));

        List<SlotResponseDto> result = slotService.getAllAvailableSlots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("UNBOOKED");
    }
}
