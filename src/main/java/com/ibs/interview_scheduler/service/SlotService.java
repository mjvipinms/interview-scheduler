package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.cache.UserCacheService;
import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.SlotRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotSummaryResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import com.ibs.interview_scheduler.entity.Slot;
import com.ibs.interview_scheduler.enums.SlotStatus;
import com.ibs.interview_scheduler.exception.CustomException;
import com.ibs.interview_scheduler.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;
    private final UserCacheService userCacheService;

    public SlotResponseDto createSlot(SlotRequestDto request) {
        log.info("Creating slot{}", request);

        try {
            if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
                throw new CustomException("Slot end time must be after start time.", HttpStatus.BAD_REQUEST);
            }
            // Check for overlapping slots
            boolean overlapExists = slotRepository.existsOverlappingSlot(request.getPanelistId(), request.getStartTime(), request.getEndTime());
            if (overlapExists) {
                throw new CustomException("Slot overlaps with an existing slot for this panelist.", HttpStatus.CONFLICT);
            }
            Slot slot = Slot.builder()
                    .panelistId(request.getPanelistId())
                    .isDeleted(false)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .createdBy(UserContext.getUserName())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(SlotStatus.UNBOOKED.toString()).build();
            return toResponse(slotRepository.save(slot), null);
        } catch (CustomException e) {
            throw new RuntimeException(e);
        }

    }

    public List<SlotResponseDto> getAllSlots() {
        log.info("Fetching all slots");
        Map<Integer, String> userList = userCacheService.getUserIdNameMap();
        return slotRepository.findAll().stream().map(slot -> toResponse(slot, userList)).toList();
    }

    public SlotResponseDto getSlotById(Integer slotId) {
        log.info("Fetching slot by id ,{}", slotId);
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
        return toResponse(slot, null);
    }

    public SlotResponseDto updateSlot(Integer slotId, SlotRequestDto request) {
        try {
            log.info("Updating slot by id ,{}", slotId);
            Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
            slot.setUpdatedAt(LocalDateTime.now());
            slot.setUpdatedBy(UserContext.getUserName());
            return toResponse(slotRepository.save(slot), null);
        } catch (RuntimeException e) {
            log.error("Exception occurred at updateSlot ,{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteSlot(Integer slotId) {
        log.info("Deleting slot by id ,{}", slotId);
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setIsDeleted(true);
        slot.setUpdatedBy(UserContext.getUserName());
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);
    }

    private SlotResponseDto toResponse(Slot slot, Map<Integer, String> userList) {
        SlotResponseDto res = new SlotResponseDto();
        res.setSlotId(slot.getSlotId());
        res.setPanelistId(slot.getPanelistId());
        res.setStartTime(slot.getStartTime());
        res.setEndTime(slot.getEndTime());
        res.setStatus(slot.getStatus());
        if (userList != null) {
            res.setPanelistName(userList.get(slot.getPanelistId()));
        }
        return res;
    }

    /**
     *
     * @param startTime slot start time
     * @param endTime   slot end time
     * @return mapping slot
     */
    public List<SlotResponseDto> getOverLappingSlot(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching available slots in a time period");
        Map<Integer, String> userList = userCacheService.getUserIdNameMap();
        List<Slot> slotsList = slotRepository.findAvailablePanelistIdsInSlot(startTime, endTime);
        return slotsList.stream().map(slot -> toResponse(slot, userList)).toList();
    }

    /**
     *
     * @param panelId logged in panellist id
     * @return list of slots
     */
    public List<SlotResponseDto> getAllSlotsByPanelID(Integer panelId) {
        log.info("Fetching all slots for a panel");
        Map<Integer, String> userList = userCacheService.getUserIdNameMap();
        List<Slot> panelSlot = slotRepository.findActiveSlotsByPanelistId(panelId);
        return panelSlot.stream().map(slot -> toResponse(slot, userList)).toList();
    }

    /**
     * Method used to update slot status while creating an interview
     *
     * @param slotId slotId of the slot
     * @param booked String value ,BOOKED
     */
    public void updateSlotStatus(Integer slotId, String booked) {
        log.info("Updating slot status with status, BOOKED");
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setStatus(booked);
        slot.setUpdatedAt(LocalDateTime.now());
        slot.setUpdatedBy(UserContext.getUserName());
    }

    /**
     *
     * @param panelId
     * @return SlotSummaryResponseDto
     */
    public SlotSummaryResponseDto getSlotSummary(Integer panelId) {
        log.info("Fetching slot summary for panel dashboard");
        try {
            int totalSlotsThisMonth = slotRepository.countTotalSlotsThisMonth(panelId);
            int appliedSlots = slotRepository.countAppliedSlotsThisMonth(panelId);

            return SlotSummaryResponseDto.builder()
                    .totalSlotsThisMonth(totalSlotsThisMonth)
                    .appliedSlots(appliedSlots)
                    .weeklyPlanSlots(2) // static for now
                    .build();
        } catch (Exception e) {
            log.error("Error occurred at getSlotSummary");
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param panelistIds list bof panel ids
     * @param startTime   start time
     * @param endTime     end time
     * @return List<SlotResponseDto>
     */
    public List<SlotResponseDto> getSlotsByPanelIdStartTimeEndTime(List<Integer> panelistIds, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching slots by panels , start time and end time");
        try {
            Map<Integer, String> userList = userCacheService.getUserIdNameMap();
            List<Slot> slots = slotRepository.findByPanelistIdInAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(panelistIds, startTime, endTime);
            return slots.stream().map(slot -> toResponse(slot, userList)).toList();
        } catch (Exception e) {
            log.error("Exception occurred in getSlotsByPanelIdStartTimeEndTime,{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return List<SlotResponseDto>
     */
    public List<SlotResponseDto> getAllAvailableSlots() {
        log.info("Fetching all available slots");
        Map<Integer, String> userList = userCacheService.getUserIdNameMap();
        return slotRepository.findAll().stream()
                .filter(s -> !s.getIsDeleted()).filter(s -> s.getStatus().equals("UNBOOKED"))
                .map(slot -> toResponse(slot, userList)).toList();
    }
}
