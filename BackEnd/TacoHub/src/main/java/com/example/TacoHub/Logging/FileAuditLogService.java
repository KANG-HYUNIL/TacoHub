package com.example.TacoHub.Logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 파일 기반 감사 로그 저장 구현체
 * - 구조화된 JSON 로그 생성
 * - 전용 AUDIT Logger 사용 (logback-spring.xml 설정)
 * - MDC를 통한 추가 컨텍스트 정보 제공
 * - 환경별 로그 라우팅 (파일/CloudWatch)
 * 
 * Note: @Primary는 AuditLogConfig에서 동적으로 선택하므로 제거
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAuditLogService implements AuditLogService {
    
    private final ObjectMapper objectMapper;
    
    // 감사 로그 전용 Logger (logback-spring.xml의 AUDIT Logger 사용)
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    
    @Override
    public void save(AuditLog auditLog) {
        try {
            // MDC에 추가 컨텍스트 정보 설정
            setMDCContext(auditLog);
            
            // 구조화된 JSON 로그 생성
            String jsonLog = objectMapper.writeValueAsString(auditLog);
            
            // 감사 로그 전용 Logger로 출력
            // logback-spring.xml 설정에 따라 파일/CloudWatch로 라우팅
            AUDIT_LOGGER.info("AUDIT_EVENT: {}", jsonLog);
            
            // 개발 환경에서는 일반 로거로도 출력 (디버깅용)
            if (log.isDebugEnabled()) {
                log.debug("=== AUDIT_DEBUG === TraceId: {}, Action: {}", 
                         auditLog.getTraceId(), auditLog.getAction());
            }
            
        } catch (Exception e) {
            log.error("감사 로그 저장 실패: traceId={}, error={}", 
                     auditLog.getTraceId(), e.getMessage(), e);
        } finally {
            // MDC 정리 (메모리 누수 방지)
            clearMDCContext();
        }
    }

    @Override
    @Async("auditLogExecutor")  // 비동기 처리를 위한 어노테이션
    public void saveAsync(AuditLog auditLog) {
        save(auditLog);
    }

    /**
     * MDC(Mapped Diagnostic Context)에 추가 컨텍스트 정보 설정
     * Logback에서 로그 패턴에 사용할 수 있는 정보 제공
     */
    private void setMDCContext(AuditLog auditLog) {
        MDC.put("traceId", auditLog.getTraceId());
        MDC.put("userId", auditLog.getUserId());
        MDC.put("userEmail", auditLog.getUserEmail());
        MDC.put("action", auditLog.getAction());
        MDC.put("className", auditLog.getClassName());
        MDC.put("methodName", auditLog.getMethodName());
        MDC.put("clientIp", auditLog.getClientIp());
        MDC.put("status", auditLog.getStatus());
    }

    /**
     * MDC 컨텍스트 정리
     * 스레드 풀 재사용 시 이전 컨텍스트가 남아있지 않도록 정리
     */
    private void clearMDCContext() {
        MDC.clear();
    }

    // 기존 파일 저장 메서드들 제거 (Logback 설정으로 대체)
    // private String getLogFilePath() { ... }  - 제거됨
    // private void writeToFile(String jsonLog) { ... }  - 제거됨
}
