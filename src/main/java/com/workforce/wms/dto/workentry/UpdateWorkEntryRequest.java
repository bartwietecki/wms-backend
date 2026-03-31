package com.workforce.wms.dto.workentry;

import java.time.LocalDate;

public record UpdateWorkEntryRequest(

        LocalDate workDate,
        int minutes,
        String description
) {}
