# CustomUserDetailsService

**패키지:** com.example.TacoHub.Utils.Jwt

## 개요
- Spring Security의 UserDetailsService 구현체로, DB에서 사용자 정보를 조회하여 인증에 사용합니다.

## 주요 멤버
- `AccountRepository accountRepository`: 사용자 정보 조회

## 주요 메서드
- `loadUserByUsername(String username)`: 이메일로 사용자 조회, CustomUserDetails 반환

## 동작 흐름
1. 이메일로 AccountEntity 조회
2. CustomUserDetails로 변환하여 반환

## 예시
```java
UserDetails user = customUserDetailsService.loadUserByUsername("user@example.com");
```

## 활용
- Spring Security 인증 처리, JWT 인증
