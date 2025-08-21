# UserInfoExtractor

**패키지:** com.example.TacoHub.Logging

## 개요
- SecurityContext 및 HTTP 요청에서 현재 사용자 정보를 추출하는 유틸리티 클래스입니다.

## 주요 메서드
- `getCurrentUserId()`: 인증된 사용자 ID 반환
- `getCurrentUserEmail()`: 인증된 사용자 이메일 반환
- `getCurrentUserRole()`: 인증된 사용자 권한 반환

## 동작 흐름
1. Spring Security의 Authentication 객체에서 사용자 정보 추출
2. 인증 정보가 없거나 예외 발생 시 null 반환

## 예시
```java
String userId = userInfoExtractor.getCurrentUserId();
```

## 의존성
- Spring Security, CustomUserDetails

## 활용
- 감사 로그, 사용자 추적, 보안 감사 등
