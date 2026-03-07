package com.workforce.wms.dto.workentry;

import java.time.LocalDate;

public record CreateWorkEntryRequest (
        LocalDate workDate,
        int minutes,
        String description
) {}