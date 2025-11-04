package com.ibs.interview_scheduler.controller;


import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.requestDto.InterviewRequestDto;
import com.ibs.interview_scheduler.dtos.responseDto.InterviewResponseDto;
import com.ibs.interview_scheduler.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.ibs.interview_scheduler.utils.RoleValidator.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    static final String ACCESSSTATUSMESSAGE ="User permission denied";

    @PostMapping
    public ResponseEntity<InterviewResponseDto> createInterview(@RequestBody InterviewRequestDto request) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR"))) {
            InterviewResponseDto response = new InterviewResponseDto();
            response.setAccessStatus(ACCESSSTATUSMESSAGE);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(interviewService.createInterview(request));
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponseDto>> getAllInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDto> getInterview(@PathVariable Integer interviewId) {
        return ResponseEntity.ok(interviewService.getInterviewById(interviewId));
    }

    @PutMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDto> updateInterview(@PathVariable Integer interviewId, @RequestBody InterviewRequestDto request) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR","PANEL"))) {
            InterviewResponseDto response = new InterviewResponseDto();
            response.setAccessStatus(ACCESSSTATUSMESSAGE);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(interviewService.updateInterview(interviewId, request));
    }

    @DeleteMapping("/{interviewId}")
    public void deleteInterview(@PathVariable Integer interviewId) {
        interviewService.deleteInterview(interviewId);
    }

    @GetMapping("/panel/{panelId}")
    public ResponseEntity<List<InterviewResponseDto>> getInterviewsByPanelId(@PathVariable Integer panelId) {
        return ResponseEntity.ok(interviewService.getInterviewsByPanelId(panelId));
    }
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<InterviewResponseDto>> getInterviewsByCandidateId(@PathVariable Integer candidateId) {
        return ResponseEntity.ok(interviewService.getInterviewsByCandidateId(candidateId));
    }
    @PutMapping("/reschedule/{interviewId}")
    public ResponseEntity<InterviewResponseDto> rescheduleInterview(@PathVariable Integer interviewId, @RequestBody InterviewRequestDto request) {
        if (!isAuthorized(UserContext.getUserRole(), List.of("HR","PANEL"))) {
            InterviewResponseDto response = new InterviewResponseDto();
            response.setAccessStatus(ACCESSSTATUSMESSAGE);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(interviewService.rescheduleInterview(interviewId, request));
    }
}
