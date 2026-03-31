package com.workforce.wms.dto.workentry;

import com.workforce.wms.entity.WorkEntryStatus;

import java.time.LocalDate;

public record WorkEntryResponse (
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate workDate,
        int minutes,
        String description,
        WorkEntryStatus status
) {}
