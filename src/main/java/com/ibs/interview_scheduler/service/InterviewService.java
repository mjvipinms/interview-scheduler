package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.*;
import com.ibs.interview_scheduler.entity.Interview;
import com.ibs.interview_scheduler.enums.InterviewResult;
import com.ibs.interview_scheduler.enums.InterviewStatus;
import com.ibs.interview_scheduler.exception.CustomException;
import com.ibs.interview_scheduler.feign.UserClient;
import com.ibs.interview_scheduler.repository.InterviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final SlotService slotService;
    private final UserClient userClient;

    @Transactional
    public InterviewResponseDto createInterview(InterviewRequestDto request) {
        log.info("Creating interview, {}", request);
        try {
            validateRequestBeforeSave(request);
            Interview interview = Interview.builder()
                    .candidateId(request.getCandidateId())
                    .slotId(request.getSlotId())
                    .hrId(request.getHrId())
                    .interviewType(request.getInterviewType())
                    .result(InterviewResult.PENDING.toString())
                    .interviewStatus(InterviewStatus.CONFIRMED.toString())
                    .panelistIds(request.getPanelistIds().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")))
                    .isDeleted(false)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .createdAt(LocalDateTime.now())
                    .createdBy(UserContext.getUserName())
                    .build();

            Interview saved = interviewRepository.save(interview);
            log.info("Interview created successfully for candidate ID: {}", request.getCandidateId());

            List<SlotResponseDto> slots = slotService.getSlotsByPanelIdStartTimeEndTime(request.getPanelistIds(),
                    request.getStartTime(), request.getEndTime());
            for (SlotResponseDto slotRes : slots) {
                slotService.updateSlotStatus(slotRes.getSlotId(), "BOOKED");
                log.info("Updated Slot with ID: {}", request.getSlotId());
            }
            return toResponse(saved, null);

        } catch (CustomException ce) {
            log.warn("Interview creation validation failed: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Exception occurred while creating interview: {}", e.getMessage(), e);
            throw new CustomException("Unable to create interview due to a server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateRequestBeforeSave(InterviewRequestDto request) {
        LocalDateTime slotStart = request.getStartTime();
        LocalDateTime slotEnd = request.getEndTime();

        // Validate candidate duplicate interview
        boolean candidateAlreadyScheduled = interviewRepository.existsByCandidateIdAndStartTimeBetween(
                request.getCandidateId(),
                slotStart.minusMinutes(1),
                slotEnd.plusMinutes(1)
        );
        if (candidateAlreadyScheduled) {
            throw new CustomException("Candidate already has an interview scheduled for this time.", HttpStatus.CONFLICT);
        }

        // Validate candidate availability
        boolean candidateBusy = interviewRepository.existsByCandidateIdAndStartTimeBetween(
                request.getCandidateId(),
                slotStart.minusMinutes(1),
                slotEnd.plusMinutes(1)
        );
        if (candidateBusy) {
            throw new CustomException("Candidate is not available during this slot.", HttpStatus.BAD_REQUEST);
        }
        // Validate panel availability
        for (Integer panelId : request.getPanelistIds()) {
            boolean panelBusy = interviewRepository.existsByPanelistIdsContainingAndStartTimeBetween(
                    String.valueOf(panelId), // stored as comma-separated string
                    slotStart.minusMinutes(1),
                    slotEnd.plusMinutes(1)
            );
            if (panelBusy) {
                throw new CustomException("Panelist (ID: " + panelId + ") is not available for this slot.", HttpStatus.BAD_REQUEST);
            }
        }
    }


    public List<InterviewResponseDto> getAllInterviews() {
        log.info("Fetching all interviews");
        List<UserResponseDTO> userList = userClient.getAllUsers();
        Map<Integer, String> candidateNamesMap = userList.stream().collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
        return interviewRepository.findAll().stream()
                .map(i -> toResponse(i, candidateNamesMap)).toList();
    }

    public InterviewResponseDto getInterviewById(Integer interviewId) {
        log.info("Fetching interview by interviewId {}", interviewId);
        List<UserResponseDTO> userList = userClient.getAllUsers();
        Map<Integer, String> candidateNamesMap = userList.stream().collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        return toResponse(interview, candidateNamesMap);
    }

    public InterviewResponseDto updateInterview(Integer interviewId, InterviewRequestDto request) {
        try {
            Interview interview = interviewRepository.findById(interviewId)
                    .orElseThrow(() -> new RuntimeException("Interview not found"));
            interview.setFeedback(request.getFeedback());
            if(request.getRating() > 3){
                interview.setResult(InterviewResult.SELECTED.toString());
            }else{
                interview.setResult(InterviewResult.REJECTED.toString());
            }
            interview.setInterviewStatus(InterviewStatus.COMPLETED.toString());
            interview.setUpdatedAt(LocalDateTime.now());
            interview.setUpdatedBy(UserContext.getUserName());
            return toResponse(interviewRepository.save(interview), null);
        } catch (RuntimeException e) {
            log.error("Exception occurred at updateInterview, {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void deleteInterview(Integer interviewId) {
        log.info("Deleting interview by InterviewId, {}", interviewId);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        interview.setIsDeleted(true);
        interview.setUpdatedAt(LocalDateTime.now());
        interview.setUpdatedBy(UserContext.getUserName());
        interviewRepository.save(interview);
    }

    private InterviewResponseDto toResponse(Interview interview, Map<Integer, String> userNamesMap) {
        InterviewResponseDto res = new InterviewResponseDto();
        res.setInterviewId(interview.getInterviewId());
        res.setCandidateId(interview.getCandidateId());
        res.setSlotId(interview.getSlotId());
        res.setHrId(interview.getHrId());
        res.setInterviewType(interview.getInterviewType());
        res.setResult(interview.getResult());
        res.setFeedback(interview.getFeedback());
        res.setCreatedAt(interview.getCreatedAt());
        res.setStartTime(interview.getStartTime());
        res.setEndTime(interview.getEndTime());
        res.setInterviewStatus(interview.getInterviewStatus());
        if (userNamesMap != null && !userNamesMap.isEmpty()) {
            res.setCandidateName(userNamesMap.get(interview.getCandidateId()));
            String[] str = null;
            List<String> panelName = new ArrayList<>();
            if (interview.getPanelistIds().contains(",")) {
                str = interview.getPanelistIds().split(",");
                for (String s : str) {
                    panelName.add(userNamesMap.get(Integer.valueOf(s)));
                    res.setPanellistNames(panelName);
                }
            } else {
                panelName.add(userNamesMap.get(Integer.valueOf(interview.getPanelistIds())));
                res.setPanellistNames(panelName);
            }
        }
        return res;
    }

    /**
     *
     * @param panelId
     * @return InterviewSummaryResponseDto
     */
    public InterviewSummaryResponseDto getInterviewSummary(String panelId) {
        log.info("Collecting InterviewSummaryResponseDto for panel dashboard");
        try {
            List<UserResponseDTO> userList = userClient.getAllUsers();
            int totalAssignedThisMonth = interviewRepository.countAssignedInterviewsThisMonth(panelId);
            Map<Integer, String> candidateNameMap = userList.stream()
                    .collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextWeek = now.plusDays(7);
            List<UpcomingInterviewResponseDto> upcomingInterviews = interviewRepository
                    .findUpcomingInterviewsForWeek(panelId, now, nextWeek)
                    .stream()
                    .map(i -> UpcomingInterviewResponseDto.builder()
                            .candidateName(candidateNameMap.get(i.getCandidateId()))
                            .role(i.getInterviewType())
                            .interviewDate(i.getStartTime())
                            .mode(i.getMode())
                            .build())
                    .toList();

            return InterviewSummaryResponseDto.builder()
                    .totalAssignedThisMonth(totalAssignedThisMonth)
                    .upcomingInterviews(upcomingInterviews)
                    .build();
        } catch (Exception e) {
            log.error("Error occurred at getInterviewSummary");
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param panelId panelId
     * @return List<InterviewResponseDto>
     */
    public List<InterviewResponseDto> getInterviewsByPanelId(Integer panelId) {
        log.info("Fetching interview by passing panelId");
        try {
            List<UserResponseDTO> userList = userClient.getAllUsers();
            Map<Integer, String> candidateNamesMap = userList.stream().collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
            List<Interview> interviews = interviewRepository.findConfirmedInterviewsByPanelId(String.valueOf(panelId));
            return interviews.stream().map(i -> toResponse(i, candidateNamesMap)).toList();
        } catch (Exception e) {
            log.error("Exception occurred in getInterviewsByPanelId{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param candidateId candidateId
     * @return List<InterviewResponseDto>
     */
    public List<InterviewResponseDto> getInterviewsByCandidateId(Integer candidateId) {
        log.info("Fetching interview by passing candidateId");
        try {
          //  List<UserResponseDTO> userList = userClient.getAllUsers();
           // Map<Integer, String> candidateNamesMap = userList.stream().collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
            List<Interview> interviews = interviewRepository.findInterviewsByCandidateId(candidateId);
            return interviews.stream().map(i -> toResponse(i, null)).toList();
        } catch (Exception e) {
            log.error("Exception occurred in getInterviewsByCandidateId{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
