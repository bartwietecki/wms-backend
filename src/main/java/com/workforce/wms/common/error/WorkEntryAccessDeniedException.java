package com.workforce.wms.common.error;

public class WorkEntryAccessDeniedException extends RuntimeException {
    public WorkEntryAccessDeniedException(Long workEntryId) {
        super("Access denied to work entry: " + workEntryId);
    }
}
