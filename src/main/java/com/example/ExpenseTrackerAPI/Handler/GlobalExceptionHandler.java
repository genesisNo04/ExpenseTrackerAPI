package com.example.ExpenseTrackerAPI.Handler;

import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Exception.DuplicateUserException;
import com.example.ExpenseTrackerAPI.Exception.ResourceNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredentialsNotFound.class)
    public ResponseEntity<Map<String, String>> handleCredentialsNotFound(CredentialsNotFound ex) {
        Map<String, String> errorBody = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUserException(DuplicateUserException ex) {
        Map<String, String> errorBody = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> errorBody = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFound ex) {
        Map<String, String> errorBody = Map.of("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }
}
