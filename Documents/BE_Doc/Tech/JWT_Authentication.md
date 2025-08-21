# JWT와 Spring Security 인증/인가 시스템

## 1. 개요

TacoHub는 JWT(JSON Web Token)와 Spring Security를 조합하여 Stateless 인증/인가 시스템을 구현했습니다. 이 문서는 인증 흐름과 핵심 컴포넌트의 역할을 설명합니다.

## 2. 인증 아키텍처

### 2.1 전체 인증 흐름

```
클라이언트 요청
    ↓
Spring Security Filter Chain
    ├─ SecurityContextPersistenceFilter
    ├─ LoginFilter (로그인 시)
    ├─ JwtFilter (모든 요청)
    └─ FilterSecurityInterceptor
    ↓
Controller
    ↓
비즈니스 로직
```

### 2.2 JWT 기반 Stateless 인증

**특징:**
- 서버에 세션 상태 저장하지 않음
- 토큰 자체에 사용자 정보 포함
- 수평적 확장성 지원
- Cross-Origin 요청 지원

**토큰 구조:**
```
Header.Payload.Signature

Header: {"alg": "HS256", "typ": "JWT"}
Payload: {"sub": "user@example.com", "role": "USER", "exp": 1640995200}
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

## 3. 로그인 프로세스

### 3.1 상세 로그인 흐름

```
1. POST /login {email, password}
   ↓
2. LoginFilter.attemptAuthentication()
   ├─ 입력값 추출 및 검증
   ├─ UsernamePasswordAuthenticationToken 생성
   └─ AuthenticationManager.authenticate() 호출
   ↓
3. CustomUserDetailsService.loadUserByUsername()
   ├─ 데이터베이스에서 사용자 조회
   ├─ CustomUserDetails 객체 생성
   └─ 비밀번호 검증 (BCrypt)
   ↓
4. LoginFilter.successfulAuthentication()
   ├─ JwtUtil.generateAccessToken()
   ├─ JwtUtil.generateRefreshToken()
   ├─ Refresh Token → Redis 저장
   └─ 응답 헤더/쿠키 설정
```

### 3.2 토큰 발급

```java
// Access Token (짧은 수명)
String accessToken = Jwts.builder()
    .setSubject(userEmail)
    .claim("role", userRole)
    .claim("userId", userId)
    .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
    .compact();

// Refresh Token (긴 수명)
String refreshToken = Jwts.builder()
    .setSubject(userEmail)
    .claim("category", "refresh")
    .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
    .compact();
```

## 4. 요청 인증 프로세스

### 4.1 JwtFilter 동작

```
모든 HTTP 요청
    ↓
JwtFilter.doFilterInternal()
    ├─ Authorization 헤더에서 토큰 추출
    ├─ 토큰 유효성 검증
    │   ├─ 만료 시간 확인
    │   ├─ 서명 검증
    │   └─ 카테고리 확인 (access)
    ├─ 사용자 정보 추출
    │   ├─ subject (이메일)
    │   ├─ role (권한)
    │   └─ userId
    ├─ CustomUserDetails 생성
    ├─ Authentication 객체 생성
    └─ SecurityContextHolder에 저장
    ↓
다음 필터로 진행
```

### 4.2 SecurityContext 활용

```java
// 현재 인증된 사용자 정보 추출
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

String userId = userDetails.getAccountId();
String email = userDetails.getUsername();
String role = userDetails.getAuthorities().iterator().next().getAuthority();
```

## 5. 핵심 컴포넌트

### 5.1 LoginFilter
```java
@Component
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, 
                                              HttpServletResponse response) {
        // 로그인 요청 처리
        // 입력값 검증 및 인증 요청
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authResult) {
        // 인증 성공 시 JWT 토큰 발급
        // Redis에 Refresh Token 저장
    }
}
```

### 5.2 JwtFilter
```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {
        // 모든 요청에서 JWT 토큰 검증
        // 유효한 토큰 시 SecurityContext 설정
    }
}
```

### 5.3 CustomUserDetailsService
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 데이터베이스에서 사용자 조회
        // CustomUserDetails 객체 반환
    }
}
```

## 6. 보안 고려사항

### 6.1 토큰 관리
- **Access Token**: 10시간 (짧은 수명)
- **Refresh Token**: 57시간 (긴 수명, Redis 저장)
- **토큰 폐기**: 로그아웃 시 Redis에서 Refresh Token 제거

### 6.2 비밀번호 보안
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 6.3 CORS 및 보안 헤더
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // REST API이므로 CSRF 비활성화
        .formLogin(form -> form.disable())  // 폼 로그인 비활성화
        .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 비활성화
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless
        // ... 기타 설정
}
```

## 7. 오류 처리

### 7.1 인증 실패 시나리오

| 시나리오 | HTTP 상태 | 응답 메시지 |
|----------|-----------|------------|
| 토큰 없음 | 401 | Unauthorized |
| 토큰 만료 | 406 | access token expired |
| 잘못된 토큰 | 401 | Invalid token |
| 권한 부족 | 403 | Forbidden |

### 7.2 토큰 갱신 프로세스

```
1. Access Token 만료로 406 응답
   ↓
2. 클라이언트가 Refresh Token으로 갱신 요청
   ↓
3. JwtController.refresh()
   ├─ Refresh Token 유효성 검증
   ├─ Redis에서 토큰 존재 확인
   ├─ 새로운 Access Token 발급
   └─ 새로운 Refresh Token 발급 (로테이션)
```

## 8. 확장성 및 성능

### 8.1 Redis 활용
- **Refresh Token 저장**: 만료 시간과 함께 저장
- **세션 무효화**: 로그아웃 시 토큰 제거
- **중복 로그인 방지**: 사용자별 토큰 관리

### 8.2 다중 서버 환경
- **Stateless 특성**: 어떤 서버에서도 토큰 검증 가능
- **공유 Redis**: 모든 서버가 동일한 Redis 접근
- **로드 밸런싱**: 세션 sticky 불필요

## 9. 실제 사용 예시

### 9.1 로그인 요청
```http
POST /login
Content-Type: application/json

{
  "emailId": "user@example.com",
  "password": "password123"
}
```

**응답:**
```http
HTTP/1.1 200 OK
access: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Set-Cookie: refresh=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; HttpOnly; Path=/
```

### 9.2 보호된 리소스 접근
```http
GET /api/workspace
Authorization: access eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 9.3 토큰 갱신
```http
POST /refresh
Cookie: refresh=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

이 JWT 기반 인증 시스템은 현대적인 웹 애플리케이션의 보안 요구사항을 충족하면서도 확장성과 성능을 보장합니다.
