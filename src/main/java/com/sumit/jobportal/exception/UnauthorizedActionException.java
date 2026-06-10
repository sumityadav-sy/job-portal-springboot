package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends JobPortalException {

    public UnauthorizedActionException(String message) {
        super(message, HttpStatus.FORBIDDEN);   // always 403
    }
}