package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends JobPortalException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);    // always 409
    }
}