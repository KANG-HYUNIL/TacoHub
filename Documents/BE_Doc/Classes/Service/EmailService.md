# EmailService

**패키지:** com.example.TacoHub.Service

## 개요
- 이메일 전송 및 인증코드 발송을 담당하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `JavaMailSender javaMailSender`: 이메일 발송
- `AuthCodeService authCodeService`: 인증코드 생성/저장

## 주요 메서드
- `createEmailMessage(String to, String subject, String text)`: 이메일 메시지 생성
- `sendAuthCodeToEmail(String to, String purpose)`: 인증코드 이메일 발송

## 동작 흐름
1. 인증코드 생성
2. 인증코드 Redis 저장
3. 이메일 메시지 생성 및 발송

## 예시
```java
emailService.sendAuthCodeToEmail("user@example.com", "회원가입");
```

## 활용
- 회원가입, 비밀번호 재설정 등 인증 이메일 발송
