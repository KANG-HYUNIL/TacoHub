# AuditLogConfig 클래스

## 1. 개요

`AuditLogConfig`는 TacoHub의 감사 로깅 시스템 구성을 담당하는 Spring Configuration 클래스입니다. 환경별로 다른 로깅 전략을 동적으로 선택하고, 비동기 처리를 위한 스레드 풀을 설정합니다.

## 2. 클래스 정보

- **패키지**: `com.example.TacoHub.Config`
- **어노테이션**: `@Configuration`, `@EnableAsync`
- **역할**: 감사 로깅 시스템 설정 및 서비스 선택

## 3. 주요 기능

### 3.1 동적 서비스 선택

```java
@Bean
@Primary
public AuditLogService auditLogService(FileAuditLogService fileAuditLogService,
                                      S3AuditLogService s3AuditLogService,
                                      MultiAuditLogService multiAuditLogService) {
    switch (storageType.toLowerCase()) {
        case "multi": 
            return multiAuditLogService;    // CloudWatch + S3 + File
        case "file": 
            return fileAuditLogService;     // File만 저장
        case "s3-archive": 
            return s3AuditLogService;       // S3만 저장
        default: 
            return fileAuditLogService;     // 기본값
    }
}
```

### 3.2 비동기 스레드 풀 설정

```java
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
```

## 4. 설정값

### 4.1 환경변수 매핑

| 설정 키 | 환경변수 | 기본값 | 설명 |
|---------|----------|--------|------|
| `audit.log.storage.type` | - | `file` | 저장 방식 선택 |

### 4.2 저장 방식별 동작

**file 모드**
- 로컬 파일 시스템에만 저장
- 개발 환경에 적합
- 빠른 처리 속도

**s3-archive 모드**
- S3에만 저장 (압축 및 아카이빙)
- 장기 보관 용도
- 비용 효율적

**multi 모드**
- 파일 + CloudWatch + S3 복합 저장
- 운영 환경 권장
- 3중 백업 보장

## 5. 의존성 관계

```
AuditLogConfig
    ├─ FileAuditLogService (주입)
    ├─ S3AuditLogService (주입)
    ├─ MultiAuditLogService (주입)
    └─ ThreadPoolTaskExecutor (생성)
```

## 6. 사용 예시

### 6.1 application.yml 설정

```yaml
# 개발 환경
audit:
  log:
    storage:
      type: file

# 운영 환경  
audit:
  log:
    storage:
      type: multi
```

### 6.2 빈 주입 및 사용

```java
@Service
public class SomeService {
    
    private final AuditLogService auditLogService;  // @Primary로 자동 선택
    
    public void someMethod() {
        AuditLog log = AuditLog.builder()...
        auditLogService.save(log);  // 설정된 구현체에 따라 동작
    }
}
```

## 7. 설계 원칙

### 7.1 Strategy Pattern
- 다양한 저장 전략을 런타임에 선택
- 새로운 저장 방식 추가 시 확장 용이

### 7.2 Factory Pattern
- `auditLogService()` 메서드가 팩토리 역할
- 설정에 따라 적절한 구현체 반환

### 7.3 Configuration as Code
- 환경변수를 통한 설정 주입
- 코드 변경 없이 동작 방식 변경 가능

## 8. 주의사항

### 8.1 @Primary 어노테이션
- 여러 `AuditLogService` 구현체 중 기본 빈을 지정
- 다른 구현체 클래스에서는 `@Primary` 제거 필요

### 8.2 비동기 처리
- `@EnableAsync`로 비동기 처리 활성화
- 메인 스레드에 영향 주지 않도록 별도 스레드 풀 사용

### 8.3 Graceful Shutdown
- `setWaitForTasksToCompleteOnShutdown(true)`: 진행 중인 작업 완료 대기
- `setAwaitTerminationSeconds(30)`: 최대 30초 대기

이 클래스는 TacoHub 감사 로깅 시스템의 중추 역할을 하며, 환경별 요구사항에 따라 유연하게 동작합니다.
