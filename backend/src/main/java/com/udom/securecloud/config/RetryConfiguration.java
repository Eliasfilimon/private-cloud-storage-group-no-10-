package com.udom.securecloud.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetryConfiguration {

    @Bean
    public RetryRegistry retryRegistry() {
        io.github.resilience4j.retry.RetryConfig config = io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(e -> isRetryable(e))
                .build();

        return RetryRegistry.of(config);
    }

    @Bean
    public Retry minioRetry(RetryRegistry registry) {
        return registry.retry("minio");
    }

    private boolean isRetryable(Throwable throwable) {
        // Retry on network-related exceptions
        return throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.net.ConnectException
                || throwable instanceof java.io.IOException
                || (throwable.getMessage() != null && throwable.getMessage().contains("timeout"));
    }
}
