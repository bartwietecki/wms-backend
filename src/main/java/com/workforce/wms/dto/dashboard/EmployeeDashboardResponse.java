package com.workforce.wms.dto.dashboard;

public record EmployeeDashboardResponse(
        int totalHoursThisMonth,
        long pendingEntriesCount,
        long approvedEntriesCount,
        long rejectedEntriesCount,
        int leaveDaysRemaining
) {}
