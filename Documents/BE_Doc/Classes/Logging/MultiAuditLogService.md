# MultiAuditLogService

**패키지:** com.example.TacoHub.Logging

## 개요
- 여러 저장소(File, S3 등)에 동시 감사 로그를 저장하는 서비스 구현체입니다.
- 장애 분리, 다중 백업, 확장성 확보 목적.

## 주요 멤버 및 의존성
- `FileAuditLogService fileAuditLogService`: 파일 저장
- `S3AuditLogService s3AuditLogService`: S3 저장

## 주요 메서드
- `save(AuditLog auditLog)`: 동기 저장(모든 저장소에 순차 저장)
- `saveAsync(AuditLog auditLog)`: 비동기 저장(모든 저장소에 비동기 저장)
- `saveToAllStorages(AuditLog auditLog)`: 내부 저장소별 저장 로직

## 동작 흐름
1. 감사 로그를 각 저장소에 순차/비동기로 저장
2. 한 저장소 실패 시 다른 저장소에 영향 없이 독립 처리

## 예시
```java
multiAuditLogService.save(auditLog);
```

## 활용
- 감사 로그의 신뢰성, 이중화, 장애 대비가 필요한 환경
