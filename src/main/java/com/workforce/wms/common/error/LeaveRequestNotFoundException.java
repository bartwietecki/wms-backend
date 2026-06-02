package com.workforce.wms.common.error;

public class LeaveRequestNotFoundException extends RuntimeException {
    public LeaveRequestNotFoundException(Long id) {
        super("LeaveRequest not found: " + id);
    }
}
