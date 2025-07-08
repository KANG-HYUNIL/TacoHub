# SecurityConfig

**패키지:** com.example.TacoHub.Config

## 개요
- Spring Security 인증/인가, JWT 필터, 패스워드 인코더 등 보안 설정을 담당하는 @Configuration 클래스입니다.

## 주요 멤버
- `authenticationConfiguration`: 인증 매니저
- `redisService`, `jwtUtil`: 인증/토큰 관리

## 주요 메서드/Bean
- `authenticationManager(...)`: 인증 매니저 Bean
- `webSecurityCustomizer()`: 특정 경로 보안 제외
- `passwordEncoder()`: 비밀번호 암호화
- `securityFilterChain(...)`: JWT 필터, 세션 정책 등 보안 필터 체인

## 동작 흐름
- JWT 기반 인증/인가, 세션 정책, 필터 체인 구성

## 활용
- API 인증/인가, JWT 기반 보안 처리
