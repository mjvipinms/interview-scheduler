package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewChangeRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewChangeRequestResponseDto;
import com.ibs.interview_scheduler.service.InterviewChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ibs.interview_scheduler.utils.RoleValidator.isAuthorized;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewChangeRequestController {

    private final InterviewChangeRequestService service;

    @PostMapping("/change-request")
    public ResponseEntity<?> createChangeRequest(
            @RequestBody InterviewChangeRequestDto dto) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("PANEL"))) {
            return ResponseEntity.status(403).body("Permission denied");
        }
        InterviewChangeRequestResponseDto response = service.createChangeRequest(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/change-request/pending")
    public ResponseEntity<?> getPendingRequests() {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR"))) {
            return ResponseEntity.status(403).body("Permission denied");
        }
        List<InterviewChangeRequestResponseDto> list = service.getPendingRequests();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/panel/change-request/{panelId}")
    public ResponseEntity<?> getPendingRequestsByPanelId(@PathVariable Integer panelId) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("PANEL"))) {
            return ResponseEntity.status(403).body("Permission denied");
        }
        List<InterviewChangeRequestResponseDto> list = service.getPendingRequestsByPanelId(panelId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/change-request/{requestId}")
    public ResponseEntity<?> updateChangeRequest(
            @PathVariable Integer requestId,
            @RequestBody InterviewChangeRequestDto dto) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR"))) {
            return ResponseEntity.status(403).body("Permission denied");
        }
        InterviewChangeRequestResponseDto response = service.updateChangeRequest(requestId, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-request/bulk-approve")
    public ResponseEntity<?> bulkApprove(@RequestBody List<Integer> interviewChangeRequestIds) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR"))) {
            return ResponseEntity.status(403).body("Permission denied");
        }
        List<InterviewChangeRequestResponseDto> updatedRequests =
                service.bulkApproveRequests(interviewChangeRequestIds);
        return ResponseEntity.ok(updatedRequests);
    }

}
