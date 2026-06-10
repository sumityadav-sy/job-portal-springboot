package com.sumit.jobportal.exception;

import java.time.LocalDateTime;

public class ApiError {

    private int status;           // e.g. 404
    private String error;         // e.g. "Not Found"
    private String message;       // e.g. "User not found with id: 5"
    private LocalDateTime timestamp;

    // Constructor — GlobalExceptionHandler will use this to build the error
    public ApiError(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now(); // captured at construction time
    }

    // Getters — Jackson needs these to serialize to JSON
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}