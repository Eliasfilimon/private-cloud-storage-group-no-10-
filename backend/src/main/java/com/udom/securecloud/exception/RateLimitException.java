package com.udom.securecloud.exception;

public class RateLimitException extends RuntimeException {
    private final String endpoint;
    private final long retryAfterSeconds;

    public RateLimitException(String message, String endpoint, long retryAfterSeconds) {
        super(message);
        this.endpoint = endpoint;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
