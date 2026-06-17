package com.udom.securecloud.exception;

public class FileOperationException extends RuntimeException {
    private final String operation;
    private final String fileName;

    public FileOperationException(String message, String operation, String fileName) {
        super(message);
        this.operation = operation;
        this.fileName = fileName;
    }

    public FileOperationException(String message, String operation, String fileName, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.fileName = fileName;
    }

    public String getOperation() {
        return operation;
    }

    public String getFileName() {
        return fileName;
    }
}
