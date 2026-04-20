package com.workforce.wms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "work_entry_status_history", indexes = {
        @Index(name = "idx_wesh_work_entry_id", columnList = "work_entry_id"),
        @Index(name = "idx_wesh_changed_at",    columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
public class WorkEntryStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_entry_id", nullable = false)
    private WorkEntry workEntry;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false, length = 20)
    private WorkEntryStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private WorkEntryStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    // Nullable because user may not exist in DB yet during transition from InMemory auth
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(length = 500)
    private String comment;
}
