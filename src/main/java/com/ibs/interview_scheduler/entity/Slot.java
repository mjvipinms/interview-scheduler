package com.ibs.interview_scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer slotId;
    private Integer panelistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String graphEventId;
    private Boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
