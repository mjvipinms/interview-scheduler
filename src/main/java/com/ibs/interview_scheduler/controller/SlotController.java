package com.ibs.interview_scheduler.controller;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.ibs.interview_scheduler.dtos.requestDto.SlotRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @PostMapping
    public ResponseEntity<SlotResponseDto> createSlot(@RequestBody SlotRequestDto request) {
        return ResponseEntity.ok(slotService.createSlot( request));
    }

    @GetMapping
    public ResponseEntity<List<SlotResponseDto>> getAllSlots() {
        return ResponseEntity.ok(slotService.getAllSlots());
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotResponseDto> getSlot(@PathVariable Integer slotId) {
        return ResponseEntity.ok(slotService.getSlotById(slotId));
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<SlotResponseDto> updateSlot(@PathVariable Integer slotId, @RequestBody SlotRequestDto request) {
        return ResponseEntity.ok(slotService.updateSlot(slotId, request));
    }

    @DeleteMapping("/{slotId}")
    public void deleteSlot(@PathVariable Integer slotId) {
        slotService.deleteSlot(slotId);
    }

    @GetMapping("/overlapping/slots")
    public ResponseEntity<List<SlotResponseDto>> getAvailablePanelists(
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<SlotResponseDto> users = slotService.getOverLappingSlot(startTime, endTime);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/panel/{panelId}")
    public ResponseEntity<List<SlotResponseDto>> getAllSlotsByPanelID(@PathVariable Integer panelId) {
        return ResponseEntity.ok(slotService.getAllSlotsByPanelID(panelId));
    }
}
