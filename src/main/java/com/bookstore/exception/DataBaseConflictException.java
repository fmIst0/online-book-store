package com.bookstore.exception;

public class DataBaseConflictException extends RuntimeException {
    public DataBaseConflictException(String message) {
        super(message);
    }

    public DataBaseConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
