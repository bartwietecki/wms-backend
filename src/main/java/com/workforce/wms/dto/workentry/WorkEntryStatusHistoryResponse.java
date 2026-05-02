package com.workforce.wms.dto.workentry;

import com.workforce.wms.entity.WorkEntryStatus;

import java.time.OffsetDateTime;

public record WorkEntryStatusHistoryResponse(
        Long id,
        WorkEntryStatus oldStatus,
        WorkEntryStatus newStatus,
        OffsetDateTime changedAt,
        String changedBy,
        String comment
) {}
