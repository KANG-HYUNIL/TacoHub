# EmailVerificationDto

**패키지:** com.example.TacoHub.Dto

## 개요
- 이메일 인증 요청/검증에 사용되는 DTO 클래스입니다.

## 주요 멤버
- `email`: 이메일 주소 (형식 검증 포함)
- `authCode`: 인증 코드
- `purpose`: 인증 목적 (회원가입, 비밀번호 찾기 등)

## 예시
```java
EmailVerificationDto dto = new EmailVerificationDto("user@example.com", "123456", "회원가입");
```

## 활용
- 이메일 인증 요청, 인증코드 검증 등
