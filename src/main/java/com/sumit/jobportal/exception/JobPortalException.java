package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;

public class JobPortalException extends RuntimeException {

    private final HttpStatus httpStatus;

    public JobPortalException(String message, HttpStatus httpStatus) {
        super(message);                  // passes message up to RuntimeException
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}