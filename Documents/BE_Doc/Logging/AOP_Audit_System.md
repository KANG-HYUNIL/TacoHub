# AOP 기반 감사 로깅 시스템

## 1. 개요

TacoHub의 AOP(Aspect-Oriented Programming) 기반 감사 로깅 시스템은 비즈니스 로직에 침투하지 않으면서도 완전한 감사 추적 기능을 제공합니다. 어노테이션 기반의 선언적 방식으로 메서드 실행을 자동으로 추적하고 구조화된 로그를 생성합니다.

## 2. AOP 아키텍처 구조

### 2.1 전체 시스템 구조

```
@AuditLogging 어노테이션
    ↓ (어노테이션 기반 Pointcut)
AuditLoggingAspect (핵심 조정자)
    ├─ UserInfoExtractor (사용자 정보 전문가)
    │   ├─ SecurityContext 접근

# TacoHub AOP 기반 감사 로깅 시스템

## 1. 도입 동기 및 설계 원칙

TacoHub는 비즈니스 도메인 추적, 법적 규정 준수, 운영 감사 목적을 위해 AOP 기반 감사 로깅 시스템을 도입했습니다. 서비스 코드의 중복 로깅, 정보 누락, 정책 변경의 어려움을 해결하고, 구조화된 JSON 로그와 환경별 저장소(파일/CloudWatch/S3)로 일관된 감사 체계를 제공합니다.

## 2. 구조와 동작 원리

### 2.1 커스텀 어노테이션
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogging {
    String action();
    boolean includeParameters() default true;
    boolean includeReturnValue() default false;
    boolean includePerformance() default true;
    boolean includeUserInfo() default true;
    boolean includeErrorDetails() default true;
}
```

### 2.2 AuditLoggingAspect 핵심 로직
```java
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {
    @Around("@annotation(auditLogging)")
    public Object performAuditLogging(ProceedingJoinPoint joinPoint, AuditLogging auditLogging) throws Throwable {
        // 사전 정보 수집
        String traceId = generateTraceId();
        long startTime = System.currentTimeMillis();
        UserInfo userInfo = userInfoExtractor.extractUserInfo();
        Map<String, Object> parameters = parameterProcessor.extractParameters(joinPoint.getSignature(), joinPoint.getArgs());
        try {
            Object result = joinPoint.proceed();
            AuditLog auditLog = createSuccessLog(traceId, userInfo, parameters, startTime, auditLogging, result);
            auditLogService.saveAsync(auditLog);
            return result;
        } catch (Exception e) {
            AuditLog auditLog = createErrorLog(traceId, userInfo, parameters, startTime, auditLogging, e);
            auditLogService.saveAsync(auditLog);
            throw e;
        }
    }
}
```

### 2.3 정보 추출 컴포넌트
```java
@Component
public class UserInfoExtractor {
    public UserInfo extractUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return UserInfo.builder()
                .userId(userDetails.getAccountId())
                .userEmail(userDetails.getUsername())
                .userRole(extractRole(userDetails))
                .clientIp(getClientIpAddress())
                .userAgent(getUserAgent())
                .sessionId(getSessionId())
                .build();
        }
        return UserInfo.anonymous();
    }
}
```

## 3. 실제 사용 예시

### 3.1 서비스 메서드 적용 예시
```java
@AuditLogging(action = "워크스페이스_생성", includeParameters = true)
public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) { ... }

@AuditLogging(action = "블록_생성", includeParameters = true)
public BlockDTO createBlock(BlockDTO blockDTO) { ... }
```

## 4. 운영 전략 및 주의사항

- 감사 로그는 파일/CloudWatch/S3에 구조화된 JSON으로 저장
- 민감정보(비밀번호, 토큰 등)는 자동 마스킹
- 환경별 logback/Parameter Store로 저장소, 정책 동적 관리
- 장애/트러블슈팅은 실제 발생 시 별도 문서로 관리

---

이 문서는 TacoHub의 AOP 감사 로깅 시스템의 도입 동기, 구조와 원리, 커스텀 어노테이션/Aspect/Service 구조, 동작 순서, 실제 사용 예시 중심으로 명확히 설명합니다. 불필요한 일반론, 미확인 확장/문제 등은 축소하였으며, 실제 운영에 필요한 핵심 정보만을 제공합니다.
    for (int i = 0; i < args.length; i++) {
        String paramName = paramNames[i];
        Object paramValue = args[i];
        
        // 민감정보 마스킹
        Object maskedValue = maskSensitiveData(paramName, paramValue);
        
        // JSON 직렬화 가능한 형태로 변환
        Object serializedValue = serializeParameter(maskedValue);
        
        parameters.put(paramName, serializedValue);
    }
    
    return parameters;
}
```

#### 민감정보 마스킹 규칙
```java
private static final Map<String, String> SENSITIVE_PATTERNS = Map.of(
    "password", "****",
    "token", "****", 
    "secret", "****",
    "key", "****",
    "authCode", "****"
);
```

### 3.5 AuditLog (통합 데이터 모델)

**위치**: `com.example.TacoHub.Logging.AuditLog`

#### JSON 구조
```json
{
  "timestamp": "2024-01-15T14:30:22.123Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "userEmail": "user@example.com", 
  "userRole": "USER",
  "clientIp": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
  "sessionId": "JSESSIONID=ABC123",
  "action": "워크스페이스_생성",
  "className": "WorkSpaceService",
  "methodName": "createWorkspaceEntity",
  "parameters": {
    "newWorkspaceName": "새 워크스페이스"
  },
  "executionTimeMs": 145,
  "status": "SUCCESS",
  "returnValue": null,
  "errorType": null,
  "errorMessage": null
}
```

## 4. SecurityContext와 AOP의 통합

### 4.1 JWT 인증과 AOP의 관계

**중요한 설계 원칙:**
- **JWT 토큰**: 사용자 신원 정보만 포함 (ID, 이메일, 권한)
- **SecurityContext**: 스레드별 격리된 인증 정보 저장소
- **AOP**: SecurityContext + HTTP Request 정보를 결합하여 완전한 감사 정보 생성

### 4.2 전체 요청 처리 흐름

```
HTTP Request (with JWT Token)
    ↓
SecurityFilterChain
    ├─ JwtFilter ← JWT 토큰 검증 및 SecurityContext 설정
    │   ├─ JWT 토큰 추출 (Header: "access")
    │   ├─ 토큰 유효성 검증 (만료, 카테고리)
    │   ├─ 사용자 정보 추출 (username, role)
    │   ├─ CustomUserDetails 생성
    │   └─ SecurityContextHolder.setAuthentication()
    └─ ...other filters
    ↓
Controller
    ↓
[AOP Proxy 개입 지점] ← 핵심!
    ↓
AuditLoggingAspect (@Around Advice 실행)
    ├─ Pre-processing
    │   ├─ SecurityContext에서 사용자 정보 추출 ← JWT에서 추출된 정보
    │   ├─ HTTP Request에서 클라이언트 정보 추출 ← 실시간 정보
    │   └─ 메서드 파라미터 추출 및 마스킹
    ├─ joinPoint.proceed() → 실제 Service 메서드 실행
    └─ Post-processing → AuditLogService.save()
    ↓
HTTP Response
```

## 5. AuditLogService 저장 계층

### 5.1 저장소별 구현체

#### FileAuditLogService
**위치**: `com.example.TacoHub.Logging.FileAuditLogService`
- **용도**: 로컬 파일 시스템에 JSON 형태로 저장
- **장점**: 빠른 접근, 네트워크 독립적
- **설정**: `audit.log.storage.type=file`

#### S3AuditLogService  
**위치**: `com.example.TacoHub.Logging.S3AuditLogService`
- **용도**: AWS S3에 압축된 형태로 장기 보관
- **장점**: 무제한 용량, 저렴한 비용, 압축 지원
- **설정**: `audit.log.storage.type=s3-archive`

#### MultiAuditLogService
**위치**: `com.example.TacoHub.Logging.MultiAuditLogService`
- **용도**: 여러 저장소에 동시 저장 (복합 전략)
- **장점**: 이중화, 각 저장소의 장점 결합
- **설정**: `audit.log.storage.type=multi`

**관련 문서**: [AuditLogConfig.md](../Classes/Config/AuditLogConfig.md)

### 5.2 동적 저장소 선택

```java
@Bean
@Primary
public AuditLogService auditLogService(
    @Value("${audit.log.storage.type:file}") String storageType,
    FileAuditLogService fileAuditLogService,
    S3AuditLogService s3AuditLogService,
    MultiAuditLogService multiAuditLogService) {
    
    return switch (storageType.toLowerCase()) {
        case "multi" -> multiAuditLogService;
        case "file" -> fileAuditLogService; 
        case "s3-archive" -> s3AuditLogService;
        default -> fileAuditLogService;
    };
}
```

## 6. 비동기 처리 및 성능 최적화

### 6.1 비동기 처리 구조

```java
@Async("auditLogExecutor")
public CompletableFuture<Void> saveAsync(AuditLog auditLog) {
    try {
        save(auditLog);
        return CompletableFuture.completedFuture(null);
    } catch (Exception e) {
        log.error("Async audit log save failed", e);
        return CompletableFuture.failedFuture(e);
    }
}
```

### 6.2 전용 스레드 풀 설정

```java
@Bean("auditLogExecutor")
public Executor auditLogExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);        // 기본 스레드 수
    executor.setMaxPoolSize(5);         // 최대 스레드 수
    executor.setQueueCapacity(100);     // 큐 크기
    executor.setThreadNamePrefix("AuditLog-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    return executor;
}
```

## 7. 오류 처리 및 복구 전략

### 7.1 저장소별 독립적 오류 처리

```java
// MultiAuditLogService의 오류 처리
@Override
public void save(AuditLog auditLog) {
    List<CompletableFuture<Void>> futures = services.stream()
        .map(service -> CompletableFuture.runAsync(() -> {
            try {
                service.save(auditLog);
                log.debug("Audit log saved successfully to {}", service.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("Failed to save audit log to {}: {}", 
                    service.getClass().getSimpleName(), e.getMessage());
            }
        }, auditLogExecutor))
        .toList();
    
    // 모든 저장소의 완료를 기다리지 않음 (비동기)
    // 일부 실패해도 다른 저장소는 계속 동작
}
```

### 7.2 우아한 성능 저하 (Graceful Degradation)

- **저장소 실패**: 하나의 저장소 실패가 전체 시스템에 영향 없음
- **AOP 실패**: 감사 로깅 실패가 비즈니스 로직 실행을 막지 않음
- **성능 영향**: 비동기 처리로 메인 로직 성능 영향 최소화

## 8. 실제 사용 예시

### 8.1 Service Layer에서의 사용

```java
@Service
@RequiredArgsConstructor
public class WorkSpaceService {
    
    // 기본 감사 로깅
    @AuditLogging(action = "워크스페이스_생성")
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        return workspaceRepository.save(WorkSpaceEntity.builder()
            .name(newWorkspaceName.trim())
            .build());
    }
    
    // 파라미터 포함 감사 로깅  
    @AuditLogging(action = "워크스페이스_수정", includeParameters = true)
    public WorkSpaceEntity updateWorkspace(UUID workspaceId, String newName) {
        WorkSpaceEntity workspace = findWorkspaceById(workspaceId);
        workspace.setName(newName);
        return workspaceRepository.save(workspace);
    }
    
    // 성능 측정 포함
    @AuditLogging(action = "워크스페이스_삭제", includePerformance = true)
    public void deleteWorkspace(UUID workspaceId) {
        WorkSpaceEntity workspace = findWorkspaceById(workspaceId);
        workspaceRepository.delete(workspace);
    }
}
```

### 8.2 생성되는 감사 로그 예시

#### 성공 케이스
```json
{
  "timestamp": "2024-01-15T14:30:22.123Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000", 
  "userId": "user123",
  "userEmail": "user@example.com",
  "userRole": "USER",
  "clientIp": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "sessionId": "JSESSIONID=ABC123",
  "action": "워크스페이스_생성",
  "className": "WorkSpaceService", 
  "methodName": "createWorkspaceEntity",
  "parameters": {
    "newWorkspaceName": "새 워크스페이스"
  },
  "executionTimeMs": 145,
  "status": "SUCCESS"
}
```

#### 오류 케이스
```json
{
  "timestamp": "2024-01-15T14:30:22.123Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "user123", 
  "action": "워크스페이스_생성",
  "className": "WorkSpaceService",
  "methodName": "createWorkspaceEntity", 
  "parameters": {
    "newWorkspaceName": "중복된 워크스페이스"
  },
  "executionTimeMs": 23,
  "status": "ERROR",
  "errorType": "DataIntegrityViolationException",
  "errorMessage": "워크스페이스 이름이 이미 존재합니다"
}
```

## 9. AOP 적용 전후 비교

### 9.1 기존 방식 (SLF4J 직접 사용)

```java
@Service
public class WorkSpaceService {
    
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        // 매번 반복되는 로깅 코드
        log.info("워크스페이스 생성 시작: name={}", newWorkspaceName);
        long startTime = System.currentTimeMillis();
        
        try {
            // 비즈니스 로직
            WorkSpaceEntity result = workspaceRepository.save(newWorkSpace);
            
            // 성공 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("워크스페이스 생성 완료: id={}, 실행시간={}ms", result.getId(), executionTime);
            return result;
            
        } catch (Exception e) {
            // 실패 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("워크스페이스 생성 실패: 실행시간={}ms, 오류={}", executionTime, e.getMessage());
            throw e;
        }
    }
}
```

**문제점:**
- 비즈니스 로직과 로깅 로직 혼재
- 코드 중복 (모든 메서드마다 반복)
- 사용자 정보, 세션 정보 수집의 일관성 부족
- 로깅 정책 변경 시 모든 메서드 수정 필요

### 9.2 AOP 적용 후

```java
@Service
public class WorkSpaceService {
    
    @AuditLogging(action = "워크스페이스_생성", includeParameters = true)
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        // 순수한 비즈니스 로직만!
        WorkSpaceEntity newWorkSpace = WorkSpaceEntity.builder()
            .name(newWorkspaceName.trim())
            .build();
            
        return workspaceRepository.save(newWorkSpace);
        
        // 로깅, 성능 측정, 사용자 정보 수집 등은 모두 AOP가 자동 처리!
    }
}
```

**개선점:**
- 비즈니스 로직과 인프라 관심사 완전 분리
- 코드 가독성 및 유지보수성 향상
- 일관된 로깅 정책 적용
- 어노테이션만으로 기능 활성화

## 10. 확장성 및 향후 계획

### 10.1 새로운 저장소 추가

```java
@Service
public class DatabaseAuditLogService implements AuditLogService {
    
    @Override
    public void save(AuditLog auditLog) {
        // 관계형 데이터베이스에 구조화된 형태로 저장
        // 실시간 쿼리 및 분석 최적화
    }
}

@Service
public class KafkaAuditLogService implements AuditLogService {
    
    @Override  
    public void save(AuditLog auditLog) {
        // Kafka로 실시간 스트리밍
        // 실시간 분석 시스템과 연동
    }
}
```

### 10.2 다양한 Aspect 추가

```java
@Aspect
public class PerformanceMonitoringAspect { ... }  // 성능 모니터링

@Aspect  
public class SecurityAspect { ... }               // 보안 검증

@Aspect
public class CacheAspect { ... }                 // 캐싱
```

이 AOP 기반 감사 로깅 시스템은 TacoHub의 비즈니스 로직을 전혀 침해하지 않으면서도 완전한 감사 추적 기능을 제공하며, Spring Security의 JWT 인증과 완벽하게 통합되어 강력하고 일관된 감사 로깅을 구현합니다.
