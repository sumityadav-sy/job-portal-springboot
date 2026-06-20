package com.sumit.jobportal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // ─── HANDLER 1: Resource Not Found (404) ──────────────────────────
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
                ApiError error = new ApiError(
                                e.getHttpStatus().value(), // 404
                                e.getHttpStatus().getReasonPhrase(), // "Not Found"
                                e.getMessage() // "User not found with id: 5"
                );
                return ResponseEntity.status(e.getHttpStatus()).body(error);
        }

        // ─── HANDLER 2: Unauthorized Action (403) ─────────────────────────
        @ExceptionHandler(UnauthorizedActionException.class)
        public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedActionException e) {
                ApiError error = new ApiError(
                                e.getHttpStatus().value(), // 403
                                e.getHttpStatus().getReasonPhrase(), // "Forbidden"
                                e.getMessage());
                return ResponseEntity.status(e.getHttpStatus()).body(error);
        }

        // ─── HANDLER 3: Duplicate Resource (409) ──────────────────────────
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException e) {
                ApiError error = new ApiError(
                                e.getHttpStatus().value(), // 409
                                e.getHttpStatus().getReasonPhrase(), // "Conflict"
                                e.getMessage());
                return ResponseEntity.status(e.getHttpStatus()).body(error);
        }

        // ─── HANDLER 4: Invalid Input (400) ───────────────────────────────
        @ExceptionHandler(InvalidInputException.class)
        public ResponseEntity<ApiError> handleInvalidInput(InvalidInputException e) {
                ApiError error = new ApiError(
                                e.getHttpStatus().value(), // 400
                                e.getHttpStatus().getReasonPhrase(), // "Bad Request"
                                e.getMessage());
                return ResponseEntity.status(e.getHttpStatus()).body(error);
        }

        // ─── HANDLER 5: Bean Validation Failures (400) ────────────────────
        // Spring throws this automatically when @Valid fails on a request body
        // e.getBindingResult() contains ALL field errors from the validation run
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException e) {

                // e.getBindingResult().getFieldErrors() → list of every field that failed
                // each FieldError has: field name ("email") + rejection reason ("must not be
                // blank")
                // we join them: "email: must not be blank, name: must not be blank"
                String message = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage) // the message from @NotBlank(message="...")
                                .collect(Collectors.joining(", "));

                ApiError error = new ApiError(
                                HttpStatus.BAD_REQUEST.value(), // 400
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), // "Bad Request"
                                message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // ─── HANDLER 6: Catch-All (500) ───────────────────────────────────
        // anything that isn't one of your custom exceptions lands here
        // hides internal details from the caller — never expose stack traces
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(Exception e) {
                ApiError error = new ApiError(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), // "Internal Server Error"
                                "An unexpected error occurred" // deliberately vague — don't leak internals
                );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        // ─── HANDLER 7: Invalid Path Variable Type (400) ──────────────────
        // thrown when @PathVariable can't be converted to expected type
        // example: /api/users/role/RECRUIT → "RECRUIT" is not a valid Role enum value
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {

                // e.getName() → the parameter name e.g. "role"
                // e.getValue() → what was actually sent e.g. "RECRUIT"
                String message = "Invalid value '" + e.getValue()
                                + "' for parameter '" + e.getName() + "'";

                ApiError error = new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // ─── HANDLER 8: Bad Credentials (401) ─────────────────────────────
        // thrown by AuthenticationManager when email/password don't match
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException e) {
                ApiError error = new ApiError(
                                HttpStatus.UNAUTHORIZED.value(), // 401
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(), // "Unauthorized"
                                "Invalid email or password" // deliberately vague — don't
                ); // tell attacker which was wrong
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Add this handler
        // ─── HANDLER 9: Access Denied / Wrong Role (403) ──────────────────
        // thrown by @PreAuthorize when the user's role doesn't match
        // e.g. JOB_SEEKER trying to POST /jobs
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
                ApiError error = new ApiError(
                                HttpStatus.FORBIDDEN.value(), // 403
                                HttpStatus.FORBIDDEN.getReasonPhrase(), // "Forbidden"
                                "You do not have permission to perform this action");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
}