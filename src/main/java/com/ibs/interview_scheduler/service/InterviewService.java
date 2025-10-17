package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.SlotResponseDto;
import com.ibs.interview_scheduler.entity.Interview;
import com.ibs.interview_scheduler.enums.InterviewResult;
import com.ibs.interview_scheduler.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final SlotService slotService;
    public InterviewResponseDto createInterview(InterviewRequestDto request) {
        log.info("Creating interview , {}", request);

        try {
            String createdBy = UserContext.getUserName();
            String role = UserContext.getUserRole();

            SlotResponseDto slotResDto =slotService.getSlotById(request.getSlotId());

            Interview interview = Interview.builder()
                    .candidateId(request.getCandidateId())
                    .slotId(request.getSlotId())
                    .hrId(request.getHrId())
                    .interviewType(request.getInterviewType())
                    .result(InterviewResult.PENDING.toString())
                    .panelist_ids(request.getPanelistIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
                    .startTime(slotResDto.getStartTime())
                    .endTime(slotResDto.getEndTime())
                    .createdAt(LocalDateTime.now())
                    .createdBy(UserContext.getUserName())
                    .build();
            return toResponse(interviewRepository.save(interview));
        } catch (Exception e) {
            log.error("Exception occurred at createInterview ,{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<InterviewResponseDto> getAllInterviews() {
        log.info("Fetching all interviews");
        return interviewRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    public InterviewResponseDto getInterviewById(Integer interviewId) {
        log.info("Fetching interview by interviewId {}",interviewId);
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        return toResponse(interview);
    }

    public InterviewResponseDto updateInterview(Integer interviewId, InterviewRequestDto request) {
        try {
            Interview interview = interviewRepository.findById(interviewId)
                    .orElseThrow(() -> new RuntimeException("Interview not found"));

            interview.setInterviewType(request.getInterviewType());
            interview.setFeedback(request.getFeedback());
            interview.setResult(request.getResult());
            interview.setUpdatedAt(LocalDateTime.now());
            interview.setUpdatedBy(UserContext.getUserName());
            return toResponse(interviewRepository.save(interview));
        } catch (RuntimeException e) {
            log.error("Exception occurred at updateInterview, {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteInterview(Integer interviewId) {
        log.info("Deleting interview by InterviewId, {}", interviewId);
        interviewRepository.deleteById(interviewId);
    }

    private InterviewResponseDto toResponse(Interview interview) {
        InterviewResponseDto res = new InterviewResponseDto();
        res.setId(interview.getInterviewId());
        res.setCandidateId(interview.getCandidateId());
        res.setSlotId(interview.getSlotId());
        res.setHrId(interview.getHrId());
        res.setInterviewType(interview.getInterviewType());
        res.setResult(interview.getResult());
        res.setFeedback(interview.getFeedback());
        res.setCreatedAt(interview.getCreatedAt());
        res.setStartTime(interview.getStartTime());
        res.setEndTime(interview.getEndTime());
        return res;
    }
}
