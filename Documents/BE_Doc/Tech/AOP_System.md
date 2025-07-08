# AOP 기반 감사 로깅 시스템

## 1. AOP 개념 및 도입 배경

### 1.1 AOP(Aspect-Oriented Programming)란?

AOP는 횡단 관심사(Cross-cutting Concerns)를 분리하여 모듈성을 높이는 프로그래밍 패러다임입니다. 비즈니스 로직과 인프라스트럭처 관심사를 분리하여 코드의 재사용성과 유지보수성을 향상시킵니다.

### 1.2 TacoHub 도입 배경

**기존 문제점:**
- 각 서비스 메서드마다 개별적으로 로깅 코드 작성
- 사용자 정보, 실행 시간, 파라미터 등 감사 정보 수집 중복
- 비즈니스 로직과 로깅 로직 혼재
- 로깅 정책 변경 시 모든 서비스 메서드 수정 필요

**AOP 도입 효과:**
- 기존 서비스 코드 수정 없이 감사 로깅 기능 추가
- 통일된 로깅 정책과 형식 적용
- 비즈니스 로직과 인프라 관심사의 완전한 분리
- 유지보수성과 확장성 대폭 향상

## 2. AOP 핵심 개념

### 2.1 주요 용어

**Aspect (관점)**
- 횡단 관심사를 모듈화한 단위
- TacoHub에서는 감사 로깅이 하나의 Aspect

**Join Point (결합점)**
- Aspect가 적용될 수 있는 지점
- Spring AOP에서는 메서드 실행 시점만 지원

**Pointcut (포인트컷)**
- Join Point 중에서 실제로 Aspect를 적용할 지점을 선별하는 표현식

**Advice (조언)**
- 실제로 실행되는 코드
- `@Around`: 메서드 실행 전후 (가장 강력함)

**Weaving (위빙)**
- Aspect와 Target 객체를 연결하여 Proxy 객체를 생성하는 과정

### 2.2 Pointcut 표현식

```java
// 어노테이션 기반 (TacoHub에서 사용)
@Pointcut("@annotation(AuditLogging)")
private void auditLoggingPointcut() {}

// 패키지 기반
@Pointcut("execution(* com.example.service.*.*(..))")
private void serviceLayerPointcut() {}

// 조합 표현식
@Pointcut("execution(* com.example.service.*.*(..)) && @annotation(AuditLogging)")
private void serviceWithAuditPointcut() {}
```

## 3. Spring AOP 동작 원리

### 3.1 프록시 패턴 기반 AOP

```
클라이언트 코드
    ↓
    Bean 요청
    ↓
Spring Container
    ↓
Proxy 객체 (런타임 생성)
    ├─ Aspect 로직 실행 (Before)
    ├─ Target 객체 메서드 호출
    └─ Aspect 로직 실행 (After)
    ↓
실제 Service 객체
```

### 3.2 CGLIB vs JDK Dynamic Proxy

**CGLIB Proxy (Spring Boot 기본값)**
- 클래스 기반 프록시 생성
- 실제 클래스를 상속받아 프록시 클래스 생성
- 인터페이스 구현 없이도 AOP 적용 가능

**JDK Dynamic Proxy**
- 인터페이스 기반 프록시 생성
- Target 클래스가 인터페이스를 구현한 경우 사용

## 4. TacoHub AOP 구현

### 4.1 @AuditLogging 어노테이션

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogging {
    String action();                    // 수행 액션명
    boolean includeParameters() default true;      // 파라미터 포함 여부
    boolean includeReturnValue() default false;    // 반환값 포함 여부
    boolean includePerformance() default true;     // 성능 측정 여부
    boolean includeUserInfo() default true;        // 사용자 정보 여부
    boolean includeErrorDetails() default true;    // 오류 상세 정보 여부
}
```

### 4.2 AuditLoggingAspect 핵심 로직

```java
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {
    
    @Around("@annotation(auditLogging)")
    public Object performAuditLogging(ProceedingJoinPoint joinPoint, 
                                    AuditLogging auditLogging) throws Throwable {
        
        // 1. 사전 처리: 정보 수집
        String traceId = generateTraceId();
        long startTime = System.currentTimeMillis();
        
        // 사용자 정보 추출 (SecurityContext)
        UserInfo userInfo = userInfoExtractor.extractUserInfo();
        
        // 파라미터 정보 추출
        Map<String, Object> parameters = parameterProcessor.extractParameters(
            joinPoint.getSignature(), joinPoint.getArgs());
        
        try {
            // 2. 실제 메서드 실행
            Object result = joinPoint.proceed();
            
            // 3. 성공 사후 처리
            AuditLog auditLog = createSuccessLog(traceId, userInfo, parameters, 
                startTime, auditLogging, result);
            auditLogService.saveAsync(auditLog);
            
            return result;
            
        } catch (Exception e) {
            // 4. 실패 사후 처리
            AuditLog auditLog = createErrorLog(traceId, userInfo, parameters, 
                startTime, auditLogging, e);
            auditLogService.saveAsync(auditLog);
            
            throw e;  // 예외를 다시 던져야 함!
        }
    }
}
```

## 5. 정보 수집 컴포넌트

### 5.1 UserInfoExtractor

```java
@Component
public class UserInfoExtractor {
    
    public UserInfo extractUserInfo() {
        // SecurityContext에서 JWT 기반 사용자 정보 추출
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
    
    private String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        
        // 프록시 환경 고려
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
}
```

### 5.2 ParameterProcessor

```java
@Component
public class ParameterProcessor {
    
    public Map<String, Object> extractParameters(Signature signature, Object[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyMap();
        }
        
        String[] parameterNames = getParameterNames(signature);
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        for (int i = 0; i < args.length; i++) {
            String paramName = (i < parameterNames.length) ? 
                parameterNames[i] : "arg" + i;
            Object value = maskSensitiveData(paramName, args[i]);
            parameters.put(paramName, serializeValue(value));
        }
        
        return parameters;
    }
    
    private Object maskSensitiveData(String paramName, Object value) {
        if (value == null) return null;
        
        String lowerParamName = paramName.toLowerCase();
        if (lowerParamName.contains("password") || 
            lowerParamName.contains("token") ||
            lowerParamName.contains("secret")) {
            return "****";
        }
        
        return value;
    }
}
```

## 6. AOP 적용 전후 비교

### 6.1 AOP 적용 전 (기존 방식)

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
            log.info("워크스페이스 생성 완료: id={}, 실행시간={}ms", 
                result.getId(), executionTime);
            return result;
            
        } catch (Exception e) {
            // 실패 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("워크스페이스 생성 실패: 실행시간={}ms, 오류={}", 
                executionTime, e.getMessage());
            throw e;
        }
    }
}
```

### 6.2 AOP 적용 후

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

## 7. AOP의 이점

### 7.1 관심사의 분리 (Separation of Concerns)
- 비즈니스 로직과 인프라 관심사 완전 분리
- 코드 가독성 및 유지보수성 향상

### 7.2 코드 재사용성
- 한 번 작성한 Aspect를 모든 서비스에 적용 가능
- 중복 코드 제거

### 7.3 정책 일관성
- 모든 메서드에 동일한 로깅 정책 적용
- 감사 기준 변경 시 Aspect만 수정하면 됨

### 7.4 비침투적 (Non-intrusive)
- 기존 코드 수정 없이 새로운 기능 추가
- 어노테이션만으로 기능 활성화

## 8. 현업에서의 AOP 활용

### 8.1 일반적인 AOP 사용 사례

**감사 로깅 (Audit Logging)**
- 사용자 행동 추적
- 규정 준수 (SOX, GDPR 등)
- 보안 모니터링

**성능 모니터링**
- 메서드별 실행 시간 측정
- 병목 지점 식별
- APM 도구와 연동

**보안**
- 권한 검증
- 입력값 검증
- 민감 데이터 암호화

**트랜잭션 관리**
- `@Transactional`의 내부 구조
- 커밋/롤백 자동 처리

**캐싱**
- `@Cacheable`의 내부 구조
- 메서드 결과 자동 캐싱

### 8.2 TacoHub에서의 실제 적용

```java
// WorkSpaceService
@AuditLogging(action = "워크스페이스_생성")
public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) { ... }

@AuditLogging(action = "워크스페이스_삭제", includeUserInfo = true)
public void deleteWorkspace(UUID workspaceId) { ... }

// BlockService  
@AuditLogging(action = "블록_생성", includeParameters = true)
public BlockDTO createBlock(BlockDTO blockDTO) { ... }

@AuditLogging(action = "블록_이동", includePerformance = true)
public BlockDTO moveBlock(UUID blockId, UUID newParentId, Integer newOrderIndex) { ... }
```

## 9. 향후 확장 계획

### 9.1 다양한 Aspect 추가

```java
@Aspect
public class PerformanceMonitoringAspect {
    @Around("@annotation(PerformanceMonitoring)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) {
        // 성능 측정 및 메트릭 수집
    }
}

@Aspect  
public class SecurityAspect {
    @Before("@annotation(RequiresPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 권한 검증
    }
}

@Aspect
public class CacheAspect {
    @Around("@annotation(Cacheable)")
    public Object handleCache(ProceedingJoinPoint joinPoint) {
        // 캐시 처리
    }
}
```

### 9.2 메트릭 및 모니터링 연동

```java
@Component
public class MetricsAspect {
    
    @Around("@annotation(Metrics)")
    public Object collectMetrics(ProceedingJoinPoint joinPoint) {
        // Micrometer, Prometheus 연동
        // 커스텀 메트릭 수집
    }
}
```

이 AOP 시스템은 TacoHub의 감사 로깅 뿐만 아니라, 향후 다양한 횡단 관심사를 효율적으로 처리할 수 있는 확장 가능한 구조를 제공합니다.
