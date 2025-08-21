# AuthCodeService

**패키지:** com.example.TacoHub.Service

## 개요
- 인증코드 생성, 검증, 저장, 삭제 등 인증 관련 기능을 제공하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `RedisService<String> redisService`: 인증코드 저장/조회
- `long authCodeExpirationMillis`: 인증코드 만료 시간(설정값)

## 주요 메서드
- `createAuthCode()`: 6자리 랜덤 인증코드 생성
- `verifyAuthCode(EmailVerificationDto dto)`: 인증코드 검증

## 동작 흐름
1. 랜덤 인증코드 생성
2. Redis에 저장 및 만료 관리
3. 사용자 입력값과 비교하여 검증

## 예시
```java
String code = authCodeService.createAuthCode();
boolean valid = authCodeService.verifyAuthCode(dto);
```

## 활용
- 회원가입, 비밀번호 재설정 등 이메일 인증이 필요한 기능
