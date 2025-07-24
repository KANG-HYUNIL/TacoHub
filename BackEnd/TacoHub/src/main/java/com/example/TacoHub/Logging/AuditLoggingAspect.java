package com.example.TacoHub.Logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AOP 기반 감사 로깅 Aspect 클래스
 * @AuditLogging 어노테이션이 붙은 메서드들을 intercept하여 자동 로깅 수행
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingAspect {

    // 의존성 주입
    private final UserInfoExtractor userInfoExtractor;
    private final ParameterProcessor parameterProcessor;
    
    private final AuditLogService auditLogService;
    
    /**
     * Pointcut 정의: @AuditLogging 어노테이션이 붙은 메서드들
     */
    @Pointcut("@annotation(AuditLogging)")
    public void auditLoggingPointcut() {
        // 포인트컷 정의만 하는 메서드 (구현체 없음)
    }
    
    /**
     * Around Advice: 메서드 실행 전후로 감사 로깅 수행
     * 
     * @param joinPoint 메서드 실행 지점 정보
     * @param auditLogging 어노테이션 정보
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("auditLoggingPointcut() && @annotation(AuditLogging)")
    public Object performAuditLogging(ProceedingJoinPoint joinPoint, AuditLogging auditLogging) throws Throwable {
        
        // === 1단계: 실행 전 준비 작업 ===
        String traceId = java.util.UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        // 메서드 정보 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        String action = auditLogging.action().isEmpty() ? methodName : auditLogging.action();
        
        // 사용자 정보 추출 (SecurityContext에서)
        String userId = null;
        String userEmail = null;
        String userRole = null;
        String clientIp = null;
        String userAgent = null;
        String sessionId = null;
        
        if (auditLogging.includeUserInfo()) {
            userId = userInfoExtractor.getCurrentUserId();
            userEmail = userInfoExtractor.getCurrentUserEmail();
            userRole = userInfoExtractor.getCurrentUserRole();
            clientIp = userInfoExtractor.getClientIpAddress();
            userAgent = userInfoExtractor.getUserAgent();
            sessionId = userInfoExtractor.getSessionId();
        }
        
        // 파라미터 정보 추출
        Map<String, Object> parameters = null;
        if (auditLogging.includeParameters()) {
            parameters = parameterProcessor.extractParameters(signature, joinPoint.getArgs());
        }
        
        // 시작 로깅
        log.info("=== 감사 로그 시작 === traceId: {}, user: {}, action: {}.{}", 
                traceId, userId, className, action);
        
        Object result = null;
        String status = "SUCCESS";
        String errorType = null;
        String errorMessage = null;
        
        try {
            // === 2단계: 실제 메서드 실행 ===
            result = joinPoint.proceed();
            
            // === 3단계: 성공 시 후처리 ===
            if (auditLogging.includePerformance()) {
                long executionTime = System.currentTimeMillis() - startTime;
                log.info("=== 감사 로그 성공 === traceId: {}, 실행시간: {}ms", traceId, executionTime);
            }
            
        } catch (Exception e) {
            // === 4단계: 예외 발생 시 처리 === TODO : 예외 처리 로직 개선 필요
            status = "ERROR";
            errorType = e.getClass().getSimpleName();
            errorMessage = e.getMessage();
            
            if (auditLogging.includeErrorDetails()) {
                log.error("=== 감사 로그 에러 === traceId: {}, errorType: {}, message: {}", 
                        traceId, errorType, errorMessage);
            }
            
            throw e; // 반드시 예외를 다시 던져야 함!
            
        } finally {
            // === 5단계: 마무리 작업 ===
            try {
                long executionTime = System.currentTimeMillis() - startTime;
                
                // AuditLog 객체 생성
                AuditLog auditLog = AuditLog.builder()
                        .traceId(traceId)
                        .sessionId(sessionId)
                        .timestamp(java.time.LocalDateTime.now())
                        .className(className)
                        .methodName(methodName)
                        .action(action)
                        .executionTimeMs(executionTime)
                        .status(status)
                        .userId(userId)
                        .userEmail(userEmail)
                        .userRole(userRole)
                        .clientIp(clientIp)
                        .userAgent(userAgent)
                        .parameters(parameters)
                        .returnValue(auditLogging.includeReturnValue() ? result : null)
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .build();
                
                // 감사 로그 저장
                auditLogService.save(auditLog);
                
            } catch (Exception logException) {
                // 로깅 실패가 메인 로직에 영향을 주면 안 됨
                log.error("감사 로그 저장 실패: {}", logException.getMessage());
            }
        }
        
        return result;
    }
    
}
