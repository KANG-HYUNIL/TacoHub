# TestAuditService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
- AOP 감사 로깅 시스템의 동작을 테스트하기 위한 예시/테스트용 서비스 클래스입니다.

## 주요 메서드
- `testBasicLogging(String message)`: 기본 감사 로깅 테스트
- `testParameterLogging(String param1, Integer param2)`: 파라미터 포함 감사 로깅 테스트
- `testExceptionLogging(boolean throwException)`: 예외 발생 감사 로깅 테스트
- `testPerformanceLogging()`: 성능 측정 감사 로깅 테스트

## 동작 흐름
- 다양한 감사 시나리오(성공, 실패, 파라미터, 반환값, 예외, 성능 등) 테스트

## 예시
```java
@AuditLogging(action = "테스트_기본_로그")
public String testBasicLogging(String message) { ... }
```

## 활용
- 감사 로깅 시스템의 실제 적용 전 검증 및 데모
