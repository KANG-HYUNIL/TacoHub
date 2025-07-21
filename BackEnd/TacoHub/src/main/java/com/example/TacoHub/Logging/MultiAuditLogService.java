package com.example.TacoHub.Logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 복합 감사 로그 저장 서비스
 * 여러 저장소에 동시에 로그를 저장 (CloudWatch + S3 + File)
 * 
 * 저장 전략:
 * - 모든 로그 → File (로컬 백업)
 * - 모든 로그 → CloudWatch (실시간 모니터링, 단기 보관)
 * - 모든 로그 → S3 (장기 보관, 압축)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "audit.log.storage.type", havingValue = "multi")
public class MultiAuditLogService implements AuditLogService {

    private final FileAuditLogService fileAuditLogService;
    private final S3AuditLogService s3AuditLogService;
    // private final CloudWatchAuditLogService cloudWatchAuditLogService; // TODO: 구현 예정

    @Override
    public void save(AuditLog auditLog) {
        // 모든 저장소에 순차적으로 저장 (동기)
        saveToAllStorages(auditLog);
    }

    @Override
    @Async("auditLogExecutor")
    public void saveAsync(AuditLog auditLog) {
        // 모든 저장소에 비동기로 저장
        saveToAllStorages(auditLog);
    }

    /**
     * 모든 저장소에 로그 저장
     * 하나의 저장소 실패가 다른 저장소에 영향을 주지 않도록 독립적으로 처리
     */
    private void saveToAllStorages(AuditLog auditLog) {
        String traceId = auditLog.getTraceId();
        
        // 1. 파일 저장 (가장 빠름, 로컬 백업)
        try {
            fileAuditLogService.save(auditLog);
            log.debug("파일 저장 성공: traceId={}", traceId);
        } catch (Exception e) {
            log.error("파일 저장 실패: traceId={}, error={}", traceId, e.getMessage());
        }

        // 2. S3 저장 (장기 보관, 압축)
        try {
            s3AuditLogService.save(auditLog);
            log.debug("S3 저장 성공: traceId={}", traceId);
        } catch (Exception e) {
            log.error("S3 저장 실패: traceId={}, error={}", traceId, e.getMessage());
        }

        // 3. CloudWatch 저장 (실시간 모니터링)
        try {
            logToCloudWatch(auditLog);
            log.debug("CloudWatch 저장 성공: traceId={}", traceId);
        } catch (Exception e) {
            log.error("CloudWatch 저장 실패: traceId={}, error={}", traceId, e.getMessage());
        } 
        
        log.debug("복합 저장 완료: traceId={}", traceId);
    }

    /**
     * CloudWatch에 감사 로그 저장
     * logback-spring.xml의 AUDIT Logger를 통해 자동으로 CloudWatch에 저장됨
     * 
     * @param auditLog 감사 로그 데이터
     */
    private void logToCloudWatch(AuditLog auditLog) {
        try {
            // AUDIT Logger를 통해 CloudWatch에 저장 (logback-spring.xml 설정)
            Logger auditLogger = LoggerFactory.getLogger("AUDIT");
            auditLogger.info("userId={}, action={}, className={}, methodName={}, clientIp={}, userAgent={}, traceId={}, timestamp={}, status={}, executionTimeMs={}, workspaceId={}, pageId={}, blockId={}", 
                auditLog.getUserId(), auditLog.getAction(), auditLog.getClassName(), auditLog.getMethodName(),
                auditLog.getClientIp(), auditLog.getUserAgent(), auditLog.getTraceId(),
                auditLog.getTimestamp(), auditLog.getStatus(), auditLog.getExecutionTimeMs(),
                auditLog.getWorkspaceId(), auditLog.getPageId(), auditLog.getBlockId());
            
            log.debug("CloudWatch 감사 로그 저장 완료: {} (User: {})", auditLog.getAction(), auditLog.getUserId());
        } catch (Exception e) {
            log.error("CloudWatch 감사 로그 저장 실패: action={}, userId={}", auditLog.getAction(), auditLog.getUserId(), e);
        }
    }

    /**
     * 병렬 저장 방식 (선택적 사용)
     * 성능은 향상되지만 에러 처리가 복잡해짐
     */
    @Async("auditLogExecutor")
    public void saveParallel(AuditLog auditLog) {
        String traceId = auditLog.getTraceId();
        
        // Runnable 리스트를 사용한 병렬 처리
        List<Runnable> tasks = List.of(
            () -> {
                try {
                    fileAuditLogService.saveAsync(auditLog);
                } catch (Exception e) {
                    log.error("파일 병렬 저장 실패: traceId={}, error={}", traceId, e.getMessage());
                }
            },
            () -> {
                try {
                    s3AuditLogService.saveAsync(auditLog);
                } catch (Exception e) {
                    log.error("S3 병렬 저장 실패: traceId={}, error={}", traceId, e.getMessage());
                }
            },
            () -> {
                try {
                    logToCloudWatch(auditLog);
                } catch (Exception e) {
                    log.error("CloudWatch 병렬 저장 실패: traceId={}, error={}", traceId, e.getMessage());
                }
            }
        );
        
        tasks.parallelStream().forEach(Runnable::run);
    }
}
