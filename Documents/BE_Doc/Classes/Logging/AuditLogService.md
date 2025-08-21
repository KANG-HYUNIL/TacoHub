# AuditLogService (인터페이스)

**패키지:** com.example.TacoHub.Logging

## 개요
- 감사 로그 저장 서비스의 공통 인터페이스입니다.
- 다양한 저장소 구현체(File, S3, Multi 등)를 위한 표준 메서드 정의.

## 주요 메서드
- `void save(AuditLog auditLog)`: 동기 저장
- `void saveAsync(AuditLog auditLog)`: 비동기 저장

## 동작 흐름
- 구현체에서 실제 저장 로직을 구현하며, AuditLoggingAspect 등에서 호출됩니다.

## 예시
```java
public class FileAuditLogService implements AuditLogService {
    public void save(AuditLog log) { ... }
    public void saveAsync(AuditLog log) { ... }
}
```

## 의존성
- AuditLog

## 활용
- 감사 로그 저장 방식 확장(파일, S3, DB 등) 시 일관된 인터페이스 제공
