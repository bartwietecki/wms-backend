package com.workforce.wms.dto.workentry;

import com.workforce.wms.entity.WorkEntryStatus;

import java.time.LocalDate;

public record WorkEntryResponse (
        Long id,
        LocalDate workDate,
        int minutes,
        String description,
        WorkEntryStatus status
) {}
