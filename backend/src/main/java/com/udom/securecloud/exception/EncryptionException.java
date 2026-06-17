package com.udom.securecloud.exception;

public class EncryptionException extends RuntimeException {
    private final String operation;

    public EncryptionException(String message, String operation) {
        super(message);
        this.operation = operation;
    }

    public EncryptionException(String message, String operation, Throwable cause) {
        super(message, cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
