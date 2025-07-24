package com.example.TacoHub.Config;

import com.example.TacoHub.Logging.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 감사 로그 구성 설정
 * application.yml의 설정에 따라 사용할 AuditLogService 구현체를 결정
 * 복합 저장 방식 지원 (CloudWatch + S3 + File)
 */
@Configuration
@EnableAsync  // 비동기 처리 활성화
@Slf4j
public class AuditLogConfig {

    @Value("${audit.log.storage.type:multi}")  // 기본값: multi
    private String storageType;

    /**
     * 감사 로그 전용 스레드 풀
     * 메인 애플리케이션 성능에 영향을 주지 않도록 별도 스레드에서 처리
     */
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 기본 스레드 수
        executor.setMaxPoolSize(5);         // 최대 스레드 수
        executor.setQueueCapacity(100);     // 큐 크기
        executor.setThreadNamePrefix("AuditLog-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 환경별 감사 로그 서비스 선택
     * 
     * application.yml 설정에 따라 동적으로 구현체 선택:
     * - multi: MultiAuditLogService (CloudWatch + S3 + File 복합 저장)
     * - file: FileAuditLogService (File 단독 저장, logback 기반)
     * - s3-archive: S3AuditLogService (S3 단독 저장, 아카이브용)
     * 
     * @Primary 어노테이션으로 기본 빈 설정
     * 각 구현체는 @Service로 등록되어 있으며, 이 메서드에서 동적 선택
     */
    @Bean
    @Primary
    public AuditLogService auditLogService(
        @Qualifier("fileAuditLogService") AuditLogService fileAuditLogService,
        @Qualifier("s3AuditLogService") AuditLogService s3AuditLogService,
        @Qualifier("multiAuditLogService") AuditLogService multiAuditLogService
    ) {
        
        switch (storageType.toLowerCase()) {
            case "multi":
                log.info("=== 복합 감사 로그 저장 활성화 === CloudWatch + S3 + File");
                return multiAuditLogService;
            case "file":
                log.info("=== 파일 감사 로그 저장 활성화 === File Only (Logback 기반)");
                return fileAuditLogService;
            case "s3-archive":
                log.info("=== S3 감사 로그 저장 활성화 === S3 Only (아카이브용)");
                return s3AuditLogService;
            default:
                log.warn("알 수 없는 저장 타입: {}, 기본값(파일) 사용", storageType);
                return fileAuditLogService;
        }
    }
}
