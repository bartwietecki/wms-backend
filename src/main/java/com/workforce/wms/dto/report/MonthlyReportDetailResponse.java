package com.workforce.wms.dto.report;

import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.MonthlyWorkReportStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record MonthlyReportDetailResponse(
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
        String adminComment,
        List<WorkEntryResponse> entries
) {}
