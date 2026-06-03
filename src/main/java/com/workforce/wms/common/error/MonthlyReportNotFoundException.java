package com.workforce.wms.common.error;

public class MonthlyReportNotFoundException extends RuntimeException {

    public MonthlyReportNotFoundException(Long id) {
        super("Monthly work report not found: " + id);
    }
}
