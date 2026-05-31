package com.udom.securecloud.security;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String limiterName = rateLimited.value();
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterName);
        
        if (rateLimiter == null) {
            log.warn("Rate limiter '{}' not found, proceeding without rate limiting", limiterName);
            return joinPoint.proceed();
        }

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for limiter: {}", limiterName);
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        }
    }
}
