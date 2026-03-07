package com.workforce.wms.common.error;

public class WorkEntryNotFoundException extends RuntimeException {
    public WorkEntryNotFoundException(Long id) {
        super("WorkEntry not found: " + id);
    }
}
