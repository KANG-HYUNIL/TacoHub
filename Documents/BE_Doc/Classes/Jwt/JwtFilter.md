# JwtFilter

**패키지:** com.example.TacoHub.Utils.Jwt

## 개요
- JWT 토큰을 검증하고 인증 정보를 SecurityContext에 등록하는 필터입니다.
- 모든 요청에 대해 1회 실행(OncePerRequestFilter 상속).

## 주요 멤버
- `JwtUtil jwtUtil`: JWT 유틸리티

## 주요 메서드
- `doFilterInternal(...)`: 토큰 추출, 만료/유효성/카테고리 검증, 인증 처리

## 동작 흐름
1. 요청 헤더에서 access 토큰 추출
2. 만료/유효성/카테고리 검증
3. 인증 성공 시 SecurityContext에 등록

## 예시
```java
filterChain.doFilter(request, response);
```

## 활용
- JWT 기반 인증이 필요한 모든 API 요청 처리
