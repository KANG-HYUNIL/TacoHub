# S3AuditLogService

**패키지:** com.example.TacoHub.Logging

## 개요
- AWS S3 기반 감사 로그 저장 구현체입니다.
- 버퍼링, 배치, 압축, 자동 업로드 등 대용량/장기 보관에 최적화.

## 주요 멤버 및 의존성
- `AmazonS3 s3Client`: S3 클라이언트
- `ObjectMapper objectMapper`: JSON 변환
- `List<AuditLog> logBuffer`: 로그 버퍼
- `String bucketName`: S3 버킷명 (설정값)
- `int batchSize`: 배치 크기 (설정값)
- `long flushInterval`: 플러시 주기 (설정값)

## 주요 메서드
- `save(AuditLog auditLog)`: 버퍼에 저장, 배치 크기 도달 시 업로드
- `flushToS3()`: 버퍼 로그를 S3로 업로드

## 동작 흐름
1. 감사 로그를 버퍼에 저장
2. 일정량/주기마다 S3로 일괄 업로드(압축 가능)

## 예시
```java
s3AuditLogService.save(auditLog);
```

## 활용
- 대용량/장기 감사 로그 보관
- 운영 환경의 효율적 감사 로그 관리
