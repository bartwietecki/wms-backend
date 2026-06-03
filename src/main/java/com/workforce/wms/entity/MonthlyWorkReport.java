package com.workforce.wms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "monthly_work_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mwr_employee_year_month",
                columnNames = {"employee_id", "year", "month"}
        ),
        indexes = {
                @Index(name = "idx_mwr_employee_id", columnList = "employee_id"),
                @Index(name = "idx_mwr_status",      columnList = "status"),
                @Index(name = "idx_mwr_year_month",  columnList = "year, month")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class MonthlyWorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MonthlyWorkReportStatus status;

    @Column(name = "total_minutes", nullable = false)
    private int totalMinutes;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "admin_comment", length = 500)
    private String adminComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
