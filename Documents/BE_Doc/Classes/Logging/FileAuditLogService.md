# FileAuditLogService

**패키지:** com.example.TacoHub.Logging

## 개요
- 파일 기반 감사 로그 저장 구현체입니다.
- 감사 로그를 JSON으로 변환하여 파일/CloudWatch 등으로 기록합니다.

## 주요 멤버 및 의존성
- `ObjectMapper objectMapper`: JSON 변환
- `Logger AUDIT_LOGGER`: 감사 로그 전용 Logger(logback-spring.xml)

## 주요 메서드
- `save(AuditLog auditLog)`: 동기 저장, JSON 변환 후 로그 기록
- `saveAsync(AuditLog auditLog)`: 비동기 저장(@Async)
- `setMDCContext()`, `clearMDCContext()`: MDC 컨텍스트 관리

## 동작 흐름
1. 감사 로그를 JSON으로 변환
2. Logger를 통해 파일/CloudWatch로 출력
3. 개발 환경에서는 일반 로그로도 출력

## 예시
```java
fileAuditLogService.save(auditLog);
```

## 활용
- 파일/CloudWatch 기반 감사 로그 저장
- 개발/운영 환경별 감사 로그 분리 관리
