package com.workforce.wms.dto.report;

import com.workforce.wms.entity.MonthlyWorkReportStatus;

import java.time.OffsetDateTime;

public record MonthlyReportResponse(
        Long id,
        Long employeeId,
        String employeeName,
        int year,
        int month,
        MonthlyWorkReportStatus status,
        int totalMinutes,
        int totalHours,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        String adminComment
) {}
