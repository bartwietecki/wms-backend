package com.workforce.wms.common.error;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found: " + id);
    }
}
