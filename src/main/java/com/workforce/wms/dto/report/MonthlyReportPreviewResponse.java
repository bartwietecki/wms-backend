package com.workforce.wms.dto.report;

import com.workforce.wms.dto.workentry.WorkEntryResponse;
import com.workforce.wms.entity.MonthlyWorkReportStatus;

import java.util.List;

public record MonthlyReportPreviewResponse(
        Long employeeId,
        String employeeName,
        int year,
        int month,
        int totalMinutes,
        int totalHours,
        int entriesCount,
        List<WorkEntryResponse> entries,
        MonthlyWorkReportStatus existingReportStatus
) {}
