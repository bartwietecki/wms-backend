package com.workforce.wms.common.error;

public class ReportAccessDeniedException extends RuntimeException {

    public ReportAccessDeniedException(Long reportId) {
        super("Access denied to report: " + reportId);
    }
}
