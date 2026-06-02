package com.workforce.wms.dto.leaverequest;

import com.workforce.wms.entity.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequestRequest(

        @NotNull
        LeaveType type,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Size(max = 500)
        String reason
) {}
