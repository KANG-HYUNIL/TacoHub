# LoginFilter

**패키지:** com.example.TacoHub.Utils.Jwt

## 개요
- 로그인 요청(/login) 처리 및 인증 성공 시 JWT 토큰을 발급하는 필터입니다.

## 주요 멤버
- `AuthenticationManager authenticationManager`: 인증 처리
- `JwtUtil jwtUtil`: JWT 유틸리티
- `RedisService<String> redisService`: 리프레시 토큰 저장

## 주요 메서드
- `attemptAuthentication(...)`: 로그인 요청 인증 시도
- (기타: 인증 성공/실패 처리, 토큰 발급 등)

## 동작 흐름
1. 로그인 요청 파싱 및 인증 시도
2. 인증 성공 시 JWT 토큰 발급 및 응답
3. 리프레시 토큰 Redis 저장

## 예시
```java
loginFilter.attemptAuthentication(request, response);
```

## 활용
- JWT 기반 로그인 처리, 토큰 발급
