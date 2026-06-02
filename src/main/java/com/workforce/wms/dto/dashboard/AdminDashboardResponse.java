package com.workforce.wms.dto.dashboard;

public record AdminDashboardResponse(
        long pendingApprovalsCount,
        long activeEmployeesCount,
        long employeesOnLeaveToday
) {}
