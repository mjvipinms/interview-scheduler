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
                WHERE s.status = 'AVAILABLE'
                  AND (
                    (s.startTime <= :endTime AND s.endTime >= :startTime)
                  ) ORDER BY s.startTime ASC
           \s""")
    List<Slot> findAvailablePanelistIdsInSlot(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Slot s WHERE s.panelistId = :panelistId ORDER BY s.startTime ASC")
    List<Slot> findActiveSlotsByPanelistId(@Param("panelistId") Integer panelistId);
}
