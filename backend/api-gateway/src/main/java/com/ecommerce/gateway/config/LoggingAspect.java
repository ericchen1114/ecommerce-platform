package com.ecommerce.gateway.config;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Gateway AOP 日誌（WebFlux 環境，不使用 ServletRequest）
 */
@Aspect
@Component
@Log4j2
public class LoggingAspect {

    @Around("execution(* com.ecommerce.gateway..*(..))")
    public Object log(ProceedingJoinPoint jp) throws Throwable {
        String handler = jp.getSignature().toShortString();
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            log.info("[GATEWAY] handler={} elapsed={}ms", handler, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("[GATEWAY] handler={} error={}", handler, t.getMessage());
            throw t;
        }
    }
}
