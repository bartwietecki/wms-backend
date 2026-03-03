package com.workforce.wms.common.error;

public class InvalidWorkEntryException extends RuntimeException {
    public InvalidWorkEntryException(String message) {
        super(message);
    }
}
