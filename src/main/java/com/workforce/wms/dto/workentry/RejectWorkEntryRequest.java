package com.workforce.wms.dto.workentry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectWorkEntryRequest(

        @NotBlank
        @Size(max = 500)
        String reason
) {}
