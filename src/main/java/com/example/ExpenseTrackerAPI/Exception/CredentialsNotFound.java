package com.example.ExpenseTrackerAPI.Exception;

public class CredentialsNotFound extends RuntimeException {
    public CredentialsNotFound(String message) {
        super(message);
    }
}
