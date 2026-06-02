package com.workforce.wms.dto.leaverequest;

import com.workforce.wms.entity.LeaveRequestStatus;
import com.workforce.wms.entity.LeaveType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType type,
        LocalDate startDate,
        LocalDate endDate,
        LeaveRequestStatus status,
        String reason,
        OffsetDateTime createdAt
) {}
