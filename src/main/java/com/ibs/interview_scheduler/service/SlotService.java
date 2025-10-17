package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.SlotRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.entity.Slot;
import com.ibs.interview_scheduler.enums.SlotStatus;
import com.ibs.interview_scheduler.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;

    private final String loggedInUsername = UserContext.getUserName();

    public SlotResponseDto createSlot(SlotRequestDto request) {
        log.info("Creating slot{}",request);
        try {
            Slot slot = Slot.builder()
                    .panelistId(request.getPanelistId())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .createdBy(loggedInUsername)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(SlotStatus.AVAILABLE.toString()).build();
            return toResponse(slotRepository.save(slot));
        } catch (Exception e) {
            log.error("Exception occurred at createSlot, {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<SlotResponseDto> getAllSlots() {
        log.info("Fetching all slots");
        return slotRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SlotResponseDto getSlotById(Integer slotId) {
        log.info("Fetching slot by id ,{}", slotId);
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
        return toResponse(slot);
    }

    public SlotResponseDto updateSlot(Integer slotId, SlotRequestDto request) {
        try {
            log.info("Updating slot by id ,{}", slotId);
            Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found"));
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
            slot.setUpdatedAt(LocalDateTime.now());
            slot.setUpdatedBy(loggedInUsername);
            return toResponse(slotRepository.save(slot));
        } catch (RuntimeException e) {
            log.error("Exception occurred at updateSlot ,"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteSlot(Integer slotId) {
        log.info("Deleting slot by id ,{}", slotId);
        slotRepository.deleteById(slotId);
    }

    private SlotResponseDto toResponse(Slot slot) {
        SlotResponseDto res = new SlotResponseDto();
        res.setId(slot.getSlotId());
        res.setPanelistId(slot.getPanelistId());
        res.setStartTime(slot.getStartTime());
        res.setEndTime(slot.getEndTime());
        res.setStatus(slot.getStatus());
        return res;
    }

    /**
     *
     * @param startTime slot start time
     * @param endTime slot end time
     * @return mapping slot
     */
    public List<SlotResponseDto> getOverLappingSlot(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching available slots in a time period");
        List<Slot> slotsList = slotRepository.findAvailablePanelistIdsInSlot(startTime, endTime);
        return slotsList.stream().map(this::toResponse).toList();
    }

    /**
     *
     * @param panelId logged in panellist id
     * @return list of slots
     */
    public List<SlotResponseDto> getAllSlotsByPanelID(Integer panelId) {
        log.info("Fetching all slots for a panel");
        List<Slot> panelSlot = slotRepository.findActiveSlotsByPanelistId(panelId);
        return panelSlot.stream().map(this::toResponse).toList();
    }
}
