package com.ibs.interview_scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer interviewId;
    private Integer candidateId;
    private Integer slotId;
    private Integer hrId;
    private String panelistIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String interviewType;
    private String interviewStatus;
    private String mode;
    private String result;
    private String feedback;
    private Boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
