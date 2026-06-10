package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends JobPortalException {

    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // always 400
    }
}