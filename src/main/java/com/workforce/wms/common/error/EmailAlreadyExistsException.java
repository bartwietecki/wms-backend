package com.workforce.wms.common.error;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Employee with email already exists: " + email);
    }
}
