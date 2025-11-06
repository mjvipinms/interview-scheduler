package com.ibs.interview_scheduler.service;

import com.ibs.interview_scheduler.cache.UserCacheService;
import com.ibs.interview_scheduler.dtos.responseDto.ReportResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import com.ibs.interview_scheduler.feign.UserClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final EntityManager entityManager;
    private final UserClient userClient;
    private final UserCacheService userCacheService;

    /**
     *
     * @param type report type
     * @param startDate start date
     * @param endDate end date
     * @param page page
     * @param size size of data
     * @param sortField sort filed
     * @param sortDir asc or desc
     * @return ReportResponseDto
     */
    public ReportResponseDto getReport(String type, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortField, String sortDir) {

        return switch (type.toLowerCase()) {
            case "interview" -> getInterviewReport(startDate, endDate, page, size, sortField, sortDir);
            case "slots" -> getSlotReport(startDate, endDate, page, size, sortField, sortDir);
            case "candidate" ->
                    userClient.getUserReport("CANDIDATE", startDate != null ? startDate.toString() : null, endDate != null ? endDate.toString() : null, page, size, sortField, sortDir);
            case "panels" ->
                    userClient.getUserReport("PANEL", startDate != null ? startDate.toString() : null, endDate != null ? endDate.toString() : null, page, size, sortField, sortDir);
            default -> throw new IllegalArgumentException("Invalid report type: " + type);
        };
    }

    /**
     *
     * @param startDate start date
     * @param endDate end date
     * @param page page
     * @param size size of data
     * @param sortField sort filed
     * @param sortDir asc or desc
     * @return ReportResponseDto
     */
    private ReportResponseDto getInterviewReport(LocalDateTime startDate, LocalDateTime endDate,
                                                 int page, int size, String sortField, String sortDir) {
        log.info("Fetching interview report for HR reports");
        try {
            List<UserResponseDTO> users = userCacheService.getAllUsers();
            Map<Integer, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));

            String sql = """
                SELECT interview_id, candidate_id, hr_id, panelist_ids, start_time, end_time, interview_status, created_at, is_deleted
                FROM interviews
                """;

            if (startDate != null && endDate != null) sql += " AND created_at BETWEEN :start AND :end";
            if (sortField != null && !sortField.isBlank()) sql += " ORDER BY " + sortField + " " + sortDir;

            Query query = entityManager.createNativeQuery(sql);
            if (startDate != null && endDate != null) {
                query.setParameter("start", startDate);
                query.setParameter("end", endDate);
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            List<Object[]> resultList = query.getResultList();

            Number total = ((Number) entityManager
                    .createNativeQuery("SELECT COUNT(*) FROM interviews ")
                    .getSingleResult());

            List<Map<String, Object>> data = resultList.stream().map(r -> {
                Integer candidateId = (Integer) r[1];
                Integer hrId = (Integer) r[2];
                String panelistIdsStr = (String) r[3];

                String candidateName = userNameMap.getOrDefault(candidateId, "Unknown");
                String hrName = userNameMap.getOrDefault(hrId, "Unknown");

                String panelNames = "";
                if (panelistIdsStr != null && !panelistIdsStr.isBlank()) {
                    panelNames = Arrays.stream(panelistIdsStr.split(","))
                            .map(String::trim)
                            .map(Integer::valueOf)
                            .map(id -> userNameMap.getOrDefault(id, "Unknown"))
                            .collect(Collectors.joining(", "));
                }

                Map<String, Object> map = new HashMap<>();
                map.put("interviewId", r[0]);
                map.put("candidateId", candidateId);
                map.put("candidateName", candidateName);
                map.put("hrId", hrId);
                map.put("hrName", hrName);
                map.put("panelistIds", panelistIdsStr);
                map.put("panelNames", panelNames);
                map.put("startTime", r[4]);
                map.put("endTime", r[5]);
                map.put("status", r[6]);
                map.put("createdAt", r[7]);
                map.put("isDeleted", r[8]);
                return map;
            }).toList();

            return new ReportResponseDto(data, page, size, total.longValue());
        } catch (Exception e) {
            log.error("Error occurred at getInterviewReport: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * @param startDate start date
     * @param endDate end date
     * @param page page
     * @param size size of data
     * @param sortField sort filed
     * @param sortDir asc or desc
     * @return ReportResponseDto
     */
    private ReportResponseDto getSlotReport(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortField, String sortDir) {
        log.info("Generating slot reports");
        try {
            String sql = "SELECT slot_id, panelist_id, start_time, end_time, status, created_at " + "FROM slots WHERE is_deleted = false";
            if (startDate != null && endDate != null) sql += " AND created_at BETWEEN :start AND :end";

            if (sortField != null && !sortField.isBlank()) sql += " ORDER BY " + sortField + " " + sortDir;

            Query query = entityManager.createNativeQuery(sql);
            if (startDate != null && endDate != null) {
                query.setParameter("start", startDate);
                query.setParameter("end", endDate);
            }

            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            List<Object[]> resultList = query.getResultList();
            Number total = ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM slots WHERE is_deleted = false").getSingleResult());

            List<Map<String, Object>> data = resultList.stream().map(r -> Map.of("slotId", r[0], "panelistId", r[1], "startTime", r[2], "endTime", r[3], "status", r[4], "createdAt", r[5])).toList();

            return new ReportResponseDto(data, page, size, total.longValue());
        } catch (Exception e) {
            log.error("Exception occurred at getSlotReport ,"+e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
