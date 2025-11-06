package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.context.UserContext;
import com.ibs.interview_scheduler.dtos.responseDto.*;
import com.ibs.interview_scheduler.enums.InterviewResult;
import com.ibs.interview_scheduler.feign.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final InterviewService interviewService;
    private final UserClient userClient;
    private final SlotService slotService;

    /**
     *
     * @return HrDashboardResponseDto
     */
    public HrDashboardResponseDto getHrDashboardSummary() {
        log.info("Creating HR dashboard");
        try {
            List<UserResponseDTO> userList = userClient.getAllUsers();
            List<InterviewResponseDto> interviews = interviewService.getAllInterviews();
            List<SlotResponseDto> slotList = slotService.getAllSlots();

            // Candidates details.
            long totalCandidates = userList.stream().filter(user -> user.getRoleId() == 4).count();
            List<Integer> candidateIds = userList.stream()
                    .filter(user -> user.getRoleId() == 4).map(UserResponseDTO::getUserId).toList();
            long assignedCount = candidateIds.stream().
                    filter(c -> interviews.stream().anyMatch(i -> Objects.equals(i.getCandidateId(), c))).count();
            long pendingCount = totalCandidates - assignedCount;

            // slot details
            long scheduled = interviews.stream().filter(i -> "CONFIRMED".equals(i.getInterviewStatus()))
                    .filter(i -> i.getStartTime().
                            isAfter(LocalDateTime.now().minusDays(1)) && i.getStartTime().isBefore(LocalDateTime.now().plusDays(4)))
                    .count();

            long availableSlots = slotList.stream().filter(slot -> Objects.equals(slot.getStatus(), "UNBOOKED")).count();
            // Panel details
            List<Integer> panelIds = userList.stream().filter(usr -> usr.getRoleId() == 3).map(UserResponseDTO::getUserId).toList();
            long pendingPanelist = panelIds.stream().filter(p -> slotList.stream()
                    .noneMatch(s -> Objects.equals(s.getPanelistId(), p))).count();

            // Upcoming interviews (next 3 days)
            Map<Integer, String> candidateNameMap = userList.stream()
                    .collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));

            List<UpcomingInterviewResponseDto> upcoming = interviews.stream().filter(i ->i.getIsDeleted() ==  false)
                    .filter(i -> i.getStartTime().
                            isAfter(LocalDateTime.now().minusDays(1)) && i.getStartTime().isBefore(LocalDateTime.now().plusDays(4))).
                    map(i -> new UpcomingInterviewResponseDto(candidateNameMap.get(i.getCandidateId()), i.getInterviewType(),
                            i.getStartTime(), i.getMode())).toList();

            long selected = interviews.stream().filter(i -> InterviewResult.SELECTED.toString().equals(i.getResult())).count();
            long rejected = interviews.stream().filter(i -> InterviewResult.REJECTED.toString().equals(i.getResult())).count();

            return HrDashboardResponseDto.builder().totalCandidates(totalCandidates).assigned(assignedCount).pending(pendingCount)
                    .scheduledInterviews(scheduled).availableSlots(availableSlots).pendingPanelists(pendingPanelist)
                    .upcoming(upcoming)
                    .selected(selected).rejected(rejected).build();
        } catch (Exception e) {
            log.error("Error occurred at getHrDashboardSummary{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return PanelDashboardResponseDto
     */
    public PanelDashboardResponseDto getPanelDashboard(Integer panelId) {
        log.info("Fetching panel dashboard details,getPanelDashboard");
        try {
            SlotSummaryResponseDto slotSummaryResponseDto = slotService.getSlotSummary(panelId);
            // Fetch interview summary
            InterviewSummaryResponseDto interviewSummaryResponseDto = interviewService.getInterviewSummary(String.valueOf(panelId));
            // Build and return the combined dashboard response
            return PanelDashboardResponseDto.builder()
                    .slotSummaryResponseDto(slotSummaryResponseDto)
                    .interviewSummaryResponseDto(interviewSummaryResponseDto)
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred in getPanelDashboard");
            throw new RuntimeException(e);
        }
    }
    /**
     *
     * @return AdminDashboardResponseDto
     */
    public AdminDashboardResponseDto getAdminDashboardSummary() {
        log.info("Fetching admin dashboard details,getAdminDashboardSummary");
        try {
            List<UserResponseDTO> userList = userClient.getAllUsers();
            long hrActive = userList.stream().filter(u -> u.getRoleName().equals("HR")).filter(UserResponseDTO::isActive).count();
            long hrInactive = userList.stream().filter(u -> u.getRoleName().equals("HR")).filter(u -> !u.isActive()).count();

            long panelActive = userList.stream().filter(u -> u.getRoleName().equals("PANEL")).filter(UserResponseDTO::isActive).count();
            long panelInactive = userList.stream().filter(u -> u.getRoleName().equals("PANEL")).filter(u -> !u.isActive()).count();

            long candidateActive = userList.stream().filter(u -> u.getRoleName().equals("CANDIDATE")).filter(UserResponseDTO::isActive).count();
            long candidateInactive = userList.stream().filter(u -> u.getRoleName().equals("CANDIDATE")).filter(u -> !u.isActive()).count();

            AdminDashboardResponseDto response = new AdminDashboardResponseDto();
            response.setHrUsers(new AdminDashboardResponseDto.CountDto(hrActive, hrInactive));
            response.setPanelists(new AdminDashboardResponseDto.CountDto(panelActive, panelInactive));
            response.setCandidates(new AdminDashboardResponseDto.CountDto(candidateActive, candidateInactive));
            return response;
        } catch (Exception e) {
            log.error("Error occurred at getAdminDashboardSummary");
            throw new RuntimeException(e);
        }
    }
}
