package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewChangeRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewChangeRequestResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewResponseDto;
import com.ibs.interview_scheduler.entity.InterviewChangeRequest;
import com.ibs.interview_scheduler.enums.RequestStatus;
import com.ibs.interview_scheduler.repository.InterviewChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewChangeRequestService {

    private final InterviewService interviewService;
    private final InterviewChangeRequestRepository changeRequestRepository;
    private final SlotService slotService;

    public InterviewChangeRequestResponseDto createChangeRequest(InterviewChangeRequestDto dto) {
        InterviewResponseDto interview = interviewService.getInterviewById(dto.getInterviewId());

        InterviewChangeRequest entity = InterviewChangeRequest.builder()
                .interviewId(interview.getInterviewId())
                .panelId(dto.getPanelId())
                .reason(dto.getReason())
                .status("PENDING")
                .createdBy(UserContext.getUserName())
                .createdAt(LocalDateTime.now())
                .build();

        InterviewChangeRequest saved = changeRequestRepository.save(entity);
        return mapToResponseDto(saved);
    }

    /**
     *
     * @return List<InterviewChangeRequestResponseDto>
     */
    public List<InterviewChangeRequestResponseDto> getPendingRequests() {
        return changeRequestRepository.findByStatus("PENDING")
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param panelId panelId
     * @return List<InterviewChangeRequestResponseDto>
     */
    public List<InterviewChangeRequestResponseDto> getPendingRequestsByPanelId(Integer panelId) {
        return changeRequestRepository.findByPanelId(panelId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param requestId chane request id
     * @param dto InterviewChangeRequestDto
     * @return InterviewChangeRequestResponseDto
     */
    public InterviewChangeRequestResponseDto updateChangeRequest(Integer requestId, InterviewChangeRequestDto dto) {
        InterviewChangeRequest req = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Change Request not found with ID: " + requestId));
        InterviewResponseDto interview = interviewService.getInterviewById(dto.getInterviewId());

        req.setStatus(dto.getStatus());
        req.setUpdatedBy(UserContext.getUserName());
        req.setUpdatedAt(LocalDateTime.now());

        if (dto.getStatus().equals(RequestStatus.APPROVED.toString())) {
            slotService.deleteSlot(interview.getSlotId());

            log.info("Updated Slot with ID: {}", interview.getSlotId());
            // Optionally delete or mark interview canceled
            interviewService.deleteInterview(interview.getInterviewId());
        }

        InterviewChangeRequest updated = changeRequestRepository.save(req);
        return mapToResponseDto(updated);
    }

    private InterviewChangeRequestResponseDto mapToResponseDto(InterviewChangeRequest entity) {
        InterviewResponseDto interviewResponseDto = null;
        try {
            // safely fetch interview details
            interviewResponseDto = interviewService.getInterviewById(entity.getInterviewId());
        } catch (Exception e) {
            log.warn("Unable to fetch interview details for ID: {}", entity.getInterviewId());
        }
        return InterviewChangeRequestResponseDto.builder()
                .interviewChangeRequestId(entity.getInterviewChangeRequestId())
                .interviewResponseDto(interviewResponseDto)
                .panelId(entity.getPanelId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .build();
    }

    /**
     *
     * @param requestIds change request id
     * @return List<InterviewChangeRequestResponseDto>
     */
    public List<InterviewChangeRequestResponseDto> bulkApproveRequests(List<Integer> requestIds) {
        log.info("Bul updating change requests");
        try {
            List<InterviewChangeRequest> requests = changeRequestRepository.findAllById(requestIds);

            List<InterviewChangeRequestResponseDto> updatedList = new ArrayList<>();
            for (InterviewChangeRequest req : requests) {
                InterviewResponseDto interview = interviewService.getInterviewById(req.getInterviewId());
                req.setStatus(RequestStatus.APPROVED.toString());
                req.setUpdatedBy(UserContext.getUserName());
                req.setUpdatedAt(LocalDateTime.now());

                slotService.deleteSlot(interview.getSlotId());
                interviewService.deleteInterview(interview.getInterviewId());

                InterviewChangeRequest updated = changeRequestRepository.save(req);
                updatedList.add(mapToResponseDto(updated));
            }
            return updatedList;
        } catch (Exception e) {
            log.error("Error occurred at bulkApproveRequests"+ e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
