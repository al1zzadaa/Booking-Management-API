package com.example.bookingmanagementapi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(* com.example.bookingmanagementapi.service..*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        log.debug("START: {}", method);

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            log.debug("SUCCESS: {} ({} ms)", method, duration);

            return result;
        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - start;

            log.error(
                    "ERROR: {} ({} ms) - {}",
                    method,
                    duration,
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }
}
