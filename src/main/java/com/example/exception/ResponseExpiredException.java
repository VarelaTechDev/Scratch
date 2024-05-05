package com.example.exception;

public class ResponseExpiredException extends RuntimeException {
    public ResponseExpiredException(String message) {
        super(message);
    }
}
