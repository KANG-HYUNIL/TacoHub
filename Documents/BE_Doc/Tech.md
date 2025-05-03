# TacoHub 기술 문서

## JWT와 Spring Security를 활용한 인증/인가 구현

### 1. 개요

TacoHub 애플리케이션은 Spring Security와 JWT(JSON Web Token)를 활용하여 사용자 인증/인가를 구현했습니다. 이 문서에서는 인증 프로세스의 동작 방식과 각 컴포넌트의 역할에 대해 상세히 설명합니다.

### 2. Spring Security란?

Spring Security는 Spring 기반 애플리케이션의 인증, 인가 및 다양한 보안 기능을 제공하는 프레임워크입니다. 인증(Authentication)은 사용자가 누구인지 확인하는 과정이고, 인가(Authorization)는 인증된 사용자가 어떤 리소스에 접근할 수 있는지를 결정하는 과정입니다.

#### 2.1 Spring Security의 주요 개념

1. **SecurityContextHolder**: 현재 인증된 사용자 정보를 저장하는 ThreadLocal 기반 저장소
2. **Authentication**: 사용자의 인증 정보를 나타내는 인터페이스
3. **AuthenticationManager**: 인증 요청을 처리하고 Authentication 객체를 생성하는 인터페이스
4. **UserDetailsService**: 사용자 정보를 로드하는 인터페이스
5. **SecurityFilterChain**: 다양한 보안 필터들을 순서에 맞게 실행하기 위한 체인

#### 2.2 Spring Security의 필터 체인

Spring Security는 필터 체인을 통해 HTTP 요청을 처리합니다. 각 필터는 특정 보안 기능을 담당하며, 필터 순서에 따라 요청이 처리됩니다. 주요 필터들은 다음과 같습니다:

1. **SecurityContextPersistenceFilter**: SecurityContext 로드/저장
2. **UsernamePasswordAuthenticationFilter**: 폼 기반 인증 처리
3. **FilterSecurityInterceptor**: 접근 제어 결정 수행

### 3. JWT (JSON Web Token)란?

JWT는 당사자 간 정보를 안전하게 전송하기 위한 개방형 표준(RFC 7519)입니다. 주로 인증 토큰으로 사용되며, 클레임이라 불리는 정보들을 JSON 형식으로 안전하게 전달할 수 있습니다.

#### 3.1 JWT 구조

JWT는 세 부분으로 구성됩니다(각 부분은 점(.)으로 구분):

1. **헤더(Header)**: 토큰 타입과 사용된 알고리즘 정보
2. **페이로드(Payload)**: 클레임(토큰에 포함된 데이터)
3. **서명(Signature)**: 토큰의 무결성을 검증하기 위한 서명

#### 3.2 JWT 장점

- 서버에 상태 저장 필요 없음(Stateless)
- 수평적 확장성 제공
- 교차 도메인 요청(CORS) 지원
- 토큰 자체에 정보 포함 가능

#### 3.3 TacoHub에서의 JWT 활용

TacoHub에서는 다음과 같이 JWT를 활용합니다:

- **Access Token**: 클라이언트 요청 인증에 사용
- **Refresh Token**: Access Token 만료 시 재발급용으로 사용

### 4. TacoHub의 로그인 및 인증 프로세스

#### 4.1 인증 흐름 개요

1. 사용자가 이메일과 비밀번호로 로그인 요청
2. Spring Security가 인증 수행
3. 인증 성공 시 Access Token과 Refresh Token 발급
4. 클라이언트는 요청 시 Access Token을 헤더에 포함하여 전송
5. 서버는 JWT 필터를 통해 토큰 검증 후 요청 처리

#### 4.2 로그인 처리 흐름 (상세)

1. 클라이언트가 `/login` 경로로 POST 요청을 보냄 (이메일 ID와 비밀번호 포함)
2. `LoginFilter`가 로그인 요청을 가로채 처리
   - 요청에서 이메일과 비밀번호 추출
   - 입력값 유효성 검증 (`InputValidator`를 통해)
   - `AuthenticationManager`에 인증 요청
3. `AuthenticationManager`가 `CustomUserDetailsService`를 통해 사용자 정보 로드
   - 데이터베이스에서 사용자 조회
   - `CustomUserDetails` 객체 생성
4. 비밀번호 검증 (BCrypt로 암호화된 비밀번호 비교)
5. 인증 성공 시 `LoginFilter`의 `successfulAuthentication` 메소드 호출
   - `JwtUtil`을 사용하여 Access Token과 Refresh Token 생성
   - Refresh Token을 Redis에 저장
   - 응답 헤더에 Access Token 추가
   - 응답 쿠키에 Refresh Token 추가

#### 4.3 요청 인증 흐름 (상세)

1. 클라이언트가 보호된 리소스에 요청 시 헤더에 Access Token 포함
2. `JwtFilter`가 모든 요청을 가로채 처리
   - 요청 헤더에서 Access Token 추출
   - 토큰이 없으면 다음 필터로 진행
3. 토큰 검증
   - 토큰 만료 여부 확인
   - 토큰 카테고리(access) 확인
4. 토큰이 유효하면 사용자 정보 추출하여 인증 객체 생성
   - 사용자명(이메일)과 권한(role) 정보 추출
   - `CustomUserDetails` 객체 생성
   - `SecurityContextHolder`에 인증 객체 저장
5. 다음 필터로 진행

### 5. 주요 클래스 및 역할

#### 5.1 인증 관련 클래스

1. **LoginFilter**
   - `UsernamePasswordAuthenticationFilter`를 확장
   - 로그인 요청을 가로채 인증 처리
   - 인증 성공 시 JWT 토큰 발급
   
2. **JwtFilter**
   - `OncePerRequestFilter`를 확장
   - 모든 요청에서 JWT 토큰 검증
   - 유효한 토큰 정보로 인증 객체 생성
   
3. **CustomUserDetailsService**
   - 데이터베이스에서 사용자 정보를 조회
   - `UserDetailsService` 인터페이스 구현
   - `loadUserByUsername` 메소드를 통해 사용자 정보 제공

4. **CustomUserDetails**
   - Spring Security의 `UserDetails` 인터페이스 구현
   - 인증에 필요한 사용자 정보 제공
   - 권한(Authorities) 정보 포함

#### 5.2 JWT 관련 클래스

1. **JwtUtil**
   - JWT 토큰 생성 및 검증 담당
   - Access Token과 Refresh Token 발급
   - 토큰에서 정보 추출 (사용자명, 권한 등)
   
#### 5.3 설정 클래스

1. **SecurityConfig**
   - Spring Security 설정 클래스
   - 필터 체인 구성
   - 인증/인가 규칙 정의
   - 비밀번호 인코더 설정
   - AuthenticationManager 설정

### 6. 코드 동작 예시

#### 6.1 로그인 요청 및 토큰 발급

```
POST /login
{
  "emailId": "user@example.com",
  "password": "1234"
}

응답:
HTTP/1.1 200 OK
access: eyJhbG...
Set-Cookie: refresh=eyJhbG...; Path=/; HttpOnly
```

#### 6.2 보호된 리소스 접근

```
GET /user/profile
Authorization: access eyJhbG...

응답:
HTTP/1.1 200 OK
{
  "user": "user@example.com",
  "name": "홍길동"
}
```

#### 6.3 만료된 토큰으로 접근 시

```
GET /user/profile
Authorization: access eyJhbG...

응답:
HTTP/1.1 406 Not Acceptable
access token expired
```

### 7. 보안 고려사항

1. **토큰 유효 기간**: Access Token은 짧게, Refresh Token은 길게 설정
2. **토큰 저장**: Refresh Token은 서버 측 Redis에 안전하게 저장
3. **HTTPS**: 모든 통신은 HTTPS로 암호화
4. **토큰 폐기**: 로그아웃 시 Refresh Token을 Redis에서 제거

### 8. 인증 흐름 다이어그램

```
클라이언트                                   서버
    |                                       |
    |--- 1. 로그인 요청 -------------------->|
    |   (email, password)                   |
    |                                       |--- 2. LoginFilter 처리
    |                                       |   - 입력값 유효성 검증
    |                                       |   - AuthenticationManager 호출
    |                                       |
    |                                       |--- 3. CustomUserDetailsService
    |                                       |   - DB에서 사용자 조회
    |                                       |   - 비밀번호 검증
    |                                       |
    |                                       |--- 4. JwtUtil
    |                                       |   - Access Token 생성
    |                                       |   - Refresh Token 생성
    |                                       |   - Refresh Token을 Redis에 저장
    |                                       |
    |<-- 5. 응답 ---------------------------|
    |   (Access Token, Refresh Token)       |
    |                                       |
    |--- 6. API 요청 ---------------------->|
    |   (Access Token 포함)                 |
    |                                       |--- 7. JwtFilter 처리
    |                                       |   - 토큰 유효성 검증
    |                                       |   - SecurityContext에 인증 정보 설정
    |                                       |
    |<-- 8. 응답 ---------------------------|
    |   (요청한 리소스)                      |
```

이 문서는 TacoHub에서 구현한 Spring Security와 JWT 기반의 인증/인가 시스템을 설명합니다. 코드의 동작 방식과 주요 컴포넌트의 역할을 이해하고, 확장 및 유지보수하는 데 도움이 될 것입니다.