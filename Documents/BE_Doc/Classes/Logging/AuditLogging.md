# AuditLogging

**패키지:** com.example.TacoHub.Logging

## 개요
- 메서드/클래스에 부착하여 자동 감사를 트리거하는 커스텀 어노테이션입니다.
- AOP 기반 감사 로깅 시스템에서 감사 대상 메서드 지정에 사용됩니다.

## 주요 멤버
- `action`: 감사 액션명(기본값: 메서드명)
- `includeParameters`: 파라미터 로깅 포함 여부(기본값: true)
- `includeReturnValue`: 반환값 로깅 포함 여부(기본값: false)
- `includePerformance`: 실행 시간 측정 포함 여부(기본값: true)
- `includeUserInfo`: 사용자 정보 포함 여부(기본값: true)
- `includeErrorDetails`: 에러 상세정보 포함 여부(기본값: true)

## 동작 흐름 및 예시
- `@AuditLogging`이 붙은 메서드는 AuditLoggingAspect에 의해 자동 감사를 수행합니다.
- 메서드 실행 전후로 AuditLog 객체가 생성되어 다양한 저장소(File/S3 등)에 기록됩니다.

### 예시
```java
@AuditLogging(action = "회원가입", includeParameters = true)
public void signUp(UserDto userDto) { ... }
```

## 의존성
- AuditLoggingAspect 등

## 활용
- 주요 API, 보안/법적 감사가 필요한 메서드에 부착하여 자동 추적 및 기록
