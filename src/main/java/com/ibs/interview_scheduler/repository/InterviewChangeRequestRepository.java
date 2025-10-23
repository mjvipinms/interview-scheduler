package com.ibs.interview_scheduler.repository;

import com.ibs.interview_scheduler.entity.InterviewChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface InterviewChangeRequestRepository extends JpaRepository<InterviewChangeRequest, Integer> {
    List<InterviewChangeRequest> findByStatus(String status);
    List<InterviewChangeRequest> findByPanelId(Integer panelId);
}
