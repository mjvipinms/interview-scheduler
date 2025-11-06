package com.ibs.interview_scheduler.repository;

import com.ibs.interview_scheduler.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {

    boolean existsByCandidateIdAndStartTimeBetween(Integer candidateId, LocalDateTime start, LocalDateTime end);

    // Since panel IDs are stored as comma-separated string:
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Interview i WHERE i.panelistIds LIKE %:panelId% " +
            "AND i.startTime BETWEEN :start AND :end")
    boolean existsByPanelistIdsContainingAndStartTimeBetween(@Param("panelId") String panelId,
                                                             @Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Interview i WHERE (CONCAT(',', i.panelistIds, ',') LIKE CONCAT('%,', :panelId, ',%')) AND MONTH(i.startTime) = MONTH(CURRENT_DATE) AND YEAR(i.startTime) = YEAR(CURRENT_DATE)")
    int countAssignedInterviewsThisMonth(@Param("panelId") String panelId);

    @Query("""
            SELECT i FROM Interview i
            WHERE CONCAT(',', i.panelistIds, ',') LIKE CONCAT('%,', :panelId, ',%')
            AND i.startTime BETWEEN :now AND :nextWeek and i.interviewStatus ='CONFIRMED'
            """)
    List<Interview> findUpcomingInterviewsForWeek(
            @Param("panelId") String panelId,
            @Param("now") LocalDateTime now,
            @Param("nextWeek") LocalDateTime nextWeek);

    @Query("SELECT i FROM Interview i " +
            "WHERE (i.panelistIds LIKE CONCAT('%,', :panelId, ',%') " +
            "   OR i.panelistIds LIKE CONCAT(:panelId, ',%') " +
            "   OR i.panelistIds LIKE CONCAT('%,', :panelId) " +
            "   OR i.panelistIds = :panelId) " +
            "AND i.interviewStatus = 'CONFIRMED' AND i.isDeleted =false")
    List<Interview> findConfirmedInterviewsByPanelId(@Param("panelId") String panelId);

    List<Interview> findInterviewsByCandidateId(Integer candidateId);
}
