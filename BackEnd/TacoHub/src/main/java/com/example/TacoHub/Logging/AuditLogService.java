package com.example.TacoHub.Logging;

/**
 * 감사 로그 저장 서비스 인터페이스
 * 다양한 저장소(파일, DB, 외부 시스템)에 대한 추상화 제공
 */
public interface AuditLogService {
    
    /**
     * 감사 로그를 저장합니다
     * @param auditLog 저장할 감사 로그
     */
    void save(AuditLog auditLog);
    
    /**
     * 감사 로그를 비동기로 저장합니다
     * @param auditLog 저장할 감사 로그
     */
    void saveAsync(AuditLog auditLog);
    
}
