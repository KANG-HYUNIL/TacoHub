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
    │   └─ HTTP Request 접근
    ├─ ParameterProcessor (파라미터 처리 전문가)
    │   ├─ 메서드 시그니처 분석
    │   ├─ 파라미터 직렬화
    │   └─ 민감정보 마스킹
    ├─ AuditLog (데이터 모델)
    │   └─ 모든 감사 정보 구조화
    └─ AuditLogService (저장 담당)
        ├─ FileAuditLogService
        ├─ S3AuditLogService
        └─ MultiAuditLogService
```

### 2.2 Spring AOP 프록시 메커니즘

```java
// 클라이언트 코드
WorkSpaceService workspaceService = applicationContext.getBean(WorkSpaceService.class);
workspaceService.createWorkspaceEntity("새 워크스페이스");

// 실제 런타임 구조
클라이언트 코드
    ↓
Proxy 객체 (Spring이 런타임에 생성)
    ├─ AuditLoggingAspect 실행 (사전 처리)
    ├─ 실제 WorkSpaceService 메서드 호출
    └─ AuditLoggingAspect 실행 (사후 처리)
    ↓
실제 WorkSpaceService 객체
```

## 3. 핵심 컴포넌트 상세

### 3.1 @AuditLogging 어노테이션

**위치**: `com.example.TacoHub.Logging.AuditLogging`

#### 설정 옵션
```java
@AuditLogging(
    action = "워크스페이스_생성",      // 로그에 표시될 액션명
    includeParameters = true,        // 파라미터 로깅 포함 여부
    includeReturnValue = false,      // 반환값 로깅 포함 여부
    includePerformance = true,       // 실행시간 측정 여부
    includeUserInfo = true,          // 사용자 정보 포함 여부
    includeErrorDetails = true       // 에러 상세정보 여부
)
```

#### 사용 예시
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
    }
}
```

### 3.2 AuditLoggingAspect (핵심 조정자)

**위치**: `com.example.TacoHub.Logging.AuditLoggingAspect`

#### @Around Advice 실행 흐름
```java
@Around("@annotation(auditLogging)")
public Object performAuditLogging(ProceedingJoinPoint joinPoint, AuditLogging auditLogging) throws Throwable {
    
    // === 1단계: 사전 처리 ===
    String traceId = UUID.randomUUID().toString();
    long startTime = System.currentTimeMillis();
    
    // 사용자 정보 추출 (SecurityContext)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = extractUserId(auth);
    
    // 클라이언트 정보 추출 (HTTP Request)
    String clientIp = userInfoExtractor.getClientIpAddress();
    
    // 파라미터 정보 추출
    Map<String, Object> parameters = parameterProcessor.extractParameters(signature, args);
    
    try {
        // === 2단계: 실제 메서드 실행 ===
        Object result = joinPoint.proceed();  // 실제 서비스 메서드 호출
        
        // === 3단계: 성공 사후 처리 ===
        long executionTime = System.currentTimeMillis() - startTime;
        
        AuditLog auditLog = AuditLog.builder()
            .traceId(traceId)
            .userId(userId)
            .clientIp(clientIp)
            .parameters(parameters)
            .executionTimeMs(executionTime)
            .status("SUCCESS")
            .build();
            
        auditLogService.save(auditLog);
        return result;
        
    } catch (Exception e) {
        // === 4단계: 예외 사후 처리 ===
        // 오류 정보도 반드시 기록하고 예외를 다시 던짐
        auditLogService.save(createErrorAuditLog(traceId, userId, e));
        throw e;  // 반드시 예외를 다시 던져야 함!
    }
}
```

**관련 문서**: [AuditLoggingAspect.md](../Classes/Logging/AuditLoggingAspect.md)

### 3.3 UserInfoExtractor (사용자/네트워크 정보 추출)

**위치**: `com.example.TacoHub.Logging.UserInfoExtractor`

#### 주요 기능
- **SecurityContext 접근**: JWT 기반 사용자 정보 추출
- **HTTP Request 접근**: 실시간 클라이언트 정보 추출
- **프록시 환경 대응**: 실제 IP 추출 로직

#### 정보 추출 과정
```java
// JWT에서 추출된 사용자 정보 (SecurityContext)
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
String userId = userDetails.getAccountId();
String userEmail = userDetails.getUsername();
String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

// HTTP Request에서 실시간 정보 추출
HttpServletRequest request = getCurrentRequest();
String clientIp = getClientIpAddress(request);  // 프록시 고려한 실제 IP
String userAgent = request.getHeader("User-Agent");
String sessionId = request.getSession().getId();
```

### 3.4 ParameterProcessor (파라미터 안전 처리)

**위치**: `com.example.TacoHub.Logging.ParameterProcessor`

#### 핵심 기능
- **메서드 시그니처 분석**: 파라미터명과 타입 매핑
- **JSON 직렬화**: 복잡한 객체의 안전한 직렬화
- **민감정보 마스킹**: 보안이 필요한 필드 자동 감지 및 마스킹

#### 파라미터 처리 과정
```java
public Map<String, Object> extractParameters(MethodSignature signature, Object[] args) {
    Map<String, Object> parameters = new HashMap<>();
    String[] paramNames = signature.getParameterNames();
    
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
