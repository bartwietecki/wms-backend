package com.workforce.wms.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectMonthlyReportRequest(
        @NotBlank
        @Size(max = 500)
        String adminComment
) {}
