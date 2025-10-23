package com.ibs.interview_scheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_change_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer interviewChangeRequestId;
    private Integer interviewId;
    private Integer panelId;
    private String reason;
    private String status;
    private String createdBy;
    private String updatedBy;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
