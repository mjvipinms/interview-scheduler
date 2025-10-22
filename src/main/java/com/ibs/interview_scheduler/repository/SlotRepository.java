package com.ibs.interview_scheduler.repository;

import com.ibs.interview_scheduler.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Integer> {

    @Query("""
                SELECT s\s
                FROM Slot s\s
                WHERE s.status = 'UNBOOKED'
                  AND (
                    (s.startTime <= :endTime AND s.endTime >= :startTime)
                  ) ORDER BY s.startTime ASC
           \s""")
    List<Slot> findAvailablePanelistIdsInSlot(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Slot s WHERE s.panelistId = :panelistId ORDER BY s.startTime ASC")
    List<Slot> findActiveSlotsByPanelistId(@Param("panelistId") Integer panelistId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Slot s " +
            "WHERE s.panelistId = :panelistId " +
            "AND (s.startTime < :newEndTime AND s.endTime > :newStartTime)")
    boolean existsOverlappingSlot(@Param("panelistId") Integer panelistId,
                                  @Param("newStartTime") LocalDateTime newStartTime,
                                  @Param("newEndTime") LocalDateTime newEndTime);

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.panelistId = :panelId " +
            "AND MONTH(s.startTime) = MONTH(CURRENT_DATE) " +
            "AND YEAR(s.startTime) = YEAR(CURRENT_DATE)")
    int countTotalSlotsThisMonth(@Param("panelId") Integer panelId);

    @Query("SELECT COUNT(s) FROM Slot s " +
            "WHERE s.panelistId = :panelId " +
            "AND s.status = 'BOOKED' " +
            "AND FUNCTION('MONTH', s.startTime) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', s.startTime) = FUNCTION('YEAR', CURRENT_DATE)")
    int countAppliedSlotsThisMonth(@Param("panelId") Integer panelId);

    List<Slot> findByPanelistIdInAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            List<Integer> panelistIds,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
