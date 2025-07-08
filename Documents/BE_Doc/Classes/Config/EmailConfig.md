# EmailConfig

**패키지:** com.example.TacoHub.Config

## 개요
- JavaMailSender 등 이메일 발송 환경을 설정하는 Spring @Configuration 클래스입니다.

## 주요 멤버
- `host`, `port`, `username`, `password`, `protocol` 등 SMTP 설정값

## 주요 메서드/Bean
- `javaMailSender()`: JavaMailSender Bean 생성

## 동작 흐름
- application.yml의 이메일 설정값을 읽어 JavaMailSender 구성

## 활용
- 인증코드, 알림 등 이메일 발송
