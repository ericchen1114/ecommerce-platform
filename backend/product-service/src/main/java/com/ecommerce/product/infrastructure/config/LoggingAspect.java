package com.ecommerce.product.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * AOP 日誌切面
 * 攔截所有 @RestController 方法，自動記錄 Request / Response
 */
@Aspect
@Component
@Log4j2
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    /**
     * 攔截所有 interfaces.rest 套件下的 Controller 方法
     */
    @Around("execution(* com.ecommerce.product.interfaces.rest..*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 取得 HTTP Request 資訊
        HttpServletRequest request = null;
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            request = attrs.getRequest();
        }

        String traceId  = UUID.randomUUID().toString().substring(0, 8);
        String userId   = request != null ? request.getHeader("X-User-Id") : "anonymous";
        String method   = request != null ? request.getMethod() : "UNKNOWN";
        String path     = request != null ? request.getRequestURI() : "UNKNOWN";
        String handler  = joinPoint.getSignature().getName();

        // 2. 設定 MDC（Log4j2 執行緒上下文，自動帶入每行 log）
        MDC.put("traceId", traceId);
        MDC.put("userId",  userId != null ? userId : "anonymous");

        // 3. 記錄 Request
        try {
            Object[] args = joinPoint.getArgs();
            String requestBody = serializeSafe(args);
            log.info("[REQUEST ] {} {} | handler={} | params={}",
                method, path, handler, requestBody);
        } catch (Exception e) {
            log.warn("無法序列化 Request 參數：{}", e.getMessage());
        }

        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            // 4. 執行原始方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;

            // 5. 記錄 Response
            if (error == null) {
                try {
                    String responseBody = serializeSafe(result);
                    log.info("[RESPONSE] {} {} | handler={} | elapsed={}ms | body={}",
                        method, path, handler, elapsed, responseBody);
                } catch (Exception e) {
                    log.warn("無法序列化 Response：{}", e.getMessage());
                }
            } else {
                log.error("[ERROR   ] {} {} | handler={} | elapsed={}ms | error={}",
                    method, path, handler, elapsed, error.getMessage(), error);
            }

            // 6. 清除 MDC，避免執行緒池污染
            MDC.clear();
        }
    }

    /**
     * 安全序列化，避免循環引用或敏感資料洩漏
     */
    private String serializeSafe(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Object[]) {
            Object[] arr = (Object[]) obj;
            if (arr.length == 0) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (Object o : arr) {
                sb.append(serializeSingleSafe(o)).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("]");
            return sb.toString();
        }
        return serializeSingleSafe(obj);
    }

    private String serializeSingleSafe(Object obj) {
        if (obj == null) return "null";
        try {
            String json = objectMapper.writeValueAsString(obj);
            // 遮蔽敏感欄位
            json = json.replaceAll("\"password\"\s*:\s*\"[^\"]*\"", "\"password\":\"***\"");
            json = json.replaceAll("\"token\"\s*:\s*\"[^\"]*\"", "\"token\":\"***\"");
            // 超過 5000 字：截斷（控制 log 大小）
            if (json.length() > 5000) {
                return json.substring(0, 5000) + "...(truncated)";
            }
            // 500–5000 字：換行輸出完整 compact JSON
            if (json.length() > 500) {
                return "\n" + json;
            }
            return json;
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
        }
    }
}
