package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends JobPortalException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);   // always 404, caller just provides message
    }
}