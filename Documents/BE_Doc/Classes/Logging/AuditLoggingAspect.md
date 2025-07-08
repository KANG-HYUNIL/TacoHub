# AuditLoggingAspect 클래스

## 1. 개요

`AuditLoggingAspect`는 TacoHub의 AOP 기반 감사 로깅 시스템의 핵심 컴포넌트입니다. `@AuditLogging` 어노테이션이 붙은 메서드를 가로채서 실행 전후에 감사 정보를 수집하고 저장합니다.

## 2. 클래스 정보

- **패키지**: `com.example.TacoHub.Logging`
- **어노테이션**: `@Aspect`, `@Component`
- **역할**: 메서드 실행 감시 및 감사 로그 생성

## 3. 핵심 기능

### 3.1 Pointcut 정의

```java
@Around("@annotation(auditLogging)")
public Object performAuditLogging(ProceedingJoinPoint joinPoint, 
                                AuditLogging auditLogging) throws Throwable {
    // @AuditLogging 어노테이션이 붙은 모든 메서드에 적용
}
```

### 3.2 감사 정보 수집

```java
// 사전 처리: 정보 수집
String traceId = UUID.randomUUID().toString();
long startTime = System.currentTimeMillis();

// 사용자 정보 추출 (SecurityContext)
UserInfo userInfo = userInfoExtractor.extractUserInfo();

// 메서드 시그니처 정보
MethodSignature signature = (MethodSignature) joinPoint.getSignature();
String className = signature.getDeclaringType().getSimpleName();
String methodName = signature.getName();

// 파라미터 정보 추출
Map<String, Object> parameters = parameterProcessor.extractParameters(signature, joinPoint.getArgs());
```

### 3.3 실행 시간 측정

```java
try {
    Object result = joinPoint.proceed();  // 실제 메서드 실행
    
    long executionTime = System.currentTimeMillis() - startTime;
    // 성공 로그 생성
    
} catch (Exception e) {
    long executionTime = System.currentTimeMillis() - startTime;
    // 실패 로그 생성
    throw e;  // 예외를 다시 던져야 함!
}
```

## 4. 의존성 관계

```
AuditLoggingAspect
    ├─ UserInfoExtractor (사용자 정보 추출)
    ├─ ParameterProcessor (파라미터 처리)
    ├─ AuditLogService (로그 저장)
    └─ ObjectMapper (JSON 직렬화)
```

## 5. 수집하는 정보

### 5.1 기본 정보
- **Trace ID**: 요청별 고유 식별자
- **실행 시간**: 메서드 실행 소요 시간 (밀리초)
- **상태**: SUCCESS/ERROR
- **타임스탬프**: 실행 시작/종료 시간

### 5.2 사용자 정보 (SecurityContext에서 추출)
- **사용자 ID**: JWT 토큰의 userId
- **이메일**: JWT 토큰의 subject
- **권한**: 사용자 역할 (USER, ADMIN 등)

### 5.3 네트워크 정보 (HTTP Request에서 추출)
- **클라이언트 IP**: X-Forwarded-For 헤더 고려
- **User-Agent**: 브라우저/클라이언트 정보
- **세션 ID**: JSESSIONID

### 5.4 메서드 정보
- **클래스명**: 실행된 서비스 클래스
- **메서드명**: 실행된 메서드
- **액션명**: @AuditLogging의 action 속성
- **파라미터**: 메서드 파라미터 (민감정보 마스킹)

### 5.5 오류 정보 (예외 발생 시)
- **오류 타입**: 예외 클래스명
- **오류 메시지**: 예외 메시지
- **스택 트레이스**: 상세 오류 정보 (선택적)

## 6. 어노테이션 설정 처리

### 6.1 @AuditLogging 속성 활용

```java
if (auditLogging.includeParameters()) {
    parameters = parameterProcessor.extractParameters(signature, joinPoint.getArgs());
}

if (auditLogging.includeReturnValue() && result != null) {
    returnValue = objectMapper.writeValueAsString(result);
}

if (auditLogging.includeUserInfo()) {
    userInfo = userInfoExtractor.extractUserInfo();
}
```

### 6.2 조건부 정보 수집
- 성능 최적화를 위해 필요한 정보만 수집
- 설정에 따라 파라미터, 반환값, 사용자 정보 등을 선택적으로 포함

## 7. 비동기 처리

```java
// 감사 로그를 비동기로 저장하여 메인 로직에 영향 없음
auditLogService.saveAsync(auditLog);
```

**장점:**
- 메인 비즈니스 로직 성능에 영향 없음
- 로그 저장 실패가 메인 로직에 영향 주지 않음
- 전용 스레드 풀에서 처리

## 8. 예외 처리

### 8.1 메서드 실행 예외
```java
catch (Exception e) {
    // 감사 로그에 오류 정보 기록
    AuditLog errorLog = createErrorLog(..., e);
    auditLogService.saveAsync(errorLog);
    
    throw e;  // 원본 예외를 반드시 다시 던져야 함!
}
```

### 8.2 로깅 자체 예외
```java
try {
    auditLogService.saveAsync(auditLog);
} catch (Exception loggingException) {
    // 로깅 실패가 메인 로직에 영향 주지 않도록 처리
    log.warn("감사 로그 저장 실패: {}", loggingException.getMessage());
}
```

## 9. 성능 고려사항

### 9.1 최적화 기법
- **비동기 처리**: 로그 저장이 메인 로직을 블록하지 않음
- **조건부 수집**: 필요한 정보만 선택적으로 수집
- **캐싱**: UserInfo 등 자주 사용되는 정보 캐싱

### 9.2 메모리 관리
- **대용량 파라미터**: JSON 직렬화 시 크기 제한
- **순환 참조**: Jackson 설정으로 순환 참조 방지
- **스레드 안전성**: ThreadLocal 기반 SecurityContext 활용

## 10. 사용 예시

### 10.1 서비스 메서드에 적용

```java
@Service
public class WorkSpaceService {
    
    @AuditLogging(
        action = "워크스페이스_생성",
        includeParameters = true,
        includePerformance = true
    )
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        // 비즈니스 로직만 작성
        return workspaceRepository.save(newWorkSpace);
        // AOP가 자동으로 감사 로그 생성
    }
}
```

### 10.2 생성되는 감사 로그

```json
{
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "userEmail": "user@example.com",
  "clientIp": "192.168.1.100",
  "action": "워크스페이스_생성",
  "className": "WorkSpaceService",
  "methodName": "createWorkspaceEntity",
  "parameters": {
    "newWorkspaceName": "새 워크스페이스"
  },
  "executionTimeMs": 145,
  "status": "SUCCESS",
  "timestamp": "2024-01-15T14:30:22.268Z"
}
```

이 클래스는 TacoHub의 모든 감사 로깅을 투명하게 처리하여, 개발자가 비즈니스 로직에만 집중할 수 있도록 합니다.
