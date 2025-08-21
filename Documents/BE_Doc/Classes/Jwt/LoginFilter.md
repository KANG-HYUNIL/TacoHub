# LoginFilter

**패키지:** com.example.TacoHub.Utils.Jwt

## 1. 개요
`LoginFilter`는 `/login` 경로로 들어오는 POST 요청을 가로채 사용자 인증을 처리하고, 성공 시 JWT 토큰을 발급하는 Spring Security 필터입니다. `UsernamePasswordAuthenticationFilter`를 상속하여 JSON 기반 로그인을 지원합니다.

## 2. 의존성 및 환경
- **Spring Security**: UsernamePasswordAuthenticationFilter, AuthenticationManager
- **Spring Framework**: HttpServletRequest, HttpServletResponse
- **TacoHub Components**: JwtUtil, RedisService, LogInDto, CustomUserDetails
- **TacoHub Validators**: InputValidator
- **TacoHub Exceptions**: InvalidLoginRequestException, TechnicalException
- **Jackson**: ObjectMapper (JSON 파싱)
- **Servlet API**: Cookie, ServletInputStream
- **Lombok**: @Slf4j

## 3. 클래스 멤버 및 의미

### 3.1 핵심 의존성
#### `authenticationManager` (AuthenticationManager, final)
- **의미**: Spring Security 인증 처리 매니저
- **역할**: 사용자 자격증명 검증 및 Authentication 객체 생성
- **주입**: 생성자 주입

#### `redisService` (RedisService<String>, final)
- **의미**: Redis 캐시 서비스
- **역할**: Refresh Token 저장 및 관리
- **주입**: 생성자 주입

#### `jwtUtil` (JwtUtil, final)
- **의미**: JWT 토큰 생성/검증 유틸리티
- **역할**: Access Token, Refresh Token 생성
- **주입**: 생성자 주입

### 3.2 상수 정의
#### `REFRESH_PREFIX` (String, static final)
- **값**: "refresh_"
- **의미**: Redis에서 Refresh Token 저장 시 키 접두사
- **용도**: "refresh_user@example.com" 형태로 사용자별 토큰 저장

#### `REFRESH_EXPIRE_TIME` (int, static final)
- **값**: 60 * 60 * 24 (1일, 초 단위)
- **의미**: Refresh Token Redis 저장 만료 시간
- **용도**: Duration.ofSeconds() 인자로 사용

#### `COOKIE_EXPIRE_TIME` (int, static final)
- **값**: 60 * 60 * 24 (1일, 초 단위)
- **의미**: Refresh Token 쿠키 만료 시간
- **용도**: Cookie.setMaxAge() 인자로 사용

#### `CONTENT_TYPE` (String, static final)
- **값**: "application/json"
- **의미**: HTTP 응답 Content-Type
- **용도**: JSON 응답 설정

#### `LOGIN_SUCCESS_MESSAGE`, `LOGIN_FAILURE_MESSAGE` (String, static final)
- **값**: JSON 형태의 성공/실패 메시지
- **의미**: 클라이언트에게 전달할 응답 메시지
- **용도**: 로그인 결과 통지

## 4. 생성자
### 4.1 `LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RedisService<String> redisService)`
- **역할**: LoginFilter 초기화 및 의존성 주입
- **인자**:
  - `authenticationManager`: 인증 처리 매니저
  - `jwtUtil`: JWT 유틸리티
  - `redisService`: Redis 서비스
- **동작**: 부모 클래스 호출 및 멤버 변수 초기화

## 5. 메서드 상세 설명

### 5.1 `Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)`
- **역할**: 로그인 시도 및 사용자 인증 처리
- **어노테이션**: @Override (UsernamePasswordAuthenticationFilter 구현)
- **인자**:
  - `request` (HttpServletRequest): 로그인 요청
  - `response` (HttpServletResponse): 응답 객체
- **반환값**: `Authentication` - 인증 성공 시 인증 객체
- **동작**:
  1. **JSON 파싱**: extractLoginDtoFromRequest()로 요청 본문에서 LogInDto 추출
  2. **입력값 검증**: validateLoginInput()으로 이메일/비밀번호 형식 검증
  3. **인증 토큰 생성**: UsernamePasswordAuthenticationToken 생성
  4. **인증 시도**: authenticationManager.authenticate() 호출
  5. **결과 반환**: 성공 시 Authentication 객체, 실패 시 예외 발생
- **예외**:
  - `InvalidLoginRequestException`: 입력값 오류, JSON 파싱 실패
  - `TechnicalException`: 시스템 오류
  - `AuthenticationException`: 인증 실패 (자격증명 불일치)

### 5.2 `LogInDto extractLoginDtoFromRequest(HttpServletRequest request)`
- **역할**: HTTP 요청 본문에서 로그인 정보 추출
- **접근성**: private (내부 헬퍼 메서드)
- **인자**: `request` (HttpServletRequest): HTTP 요청
- **반환값**: `LogInDto` - 파싱된 로그인 정보
- **동작**:
  1. **InputStream 확인**: 요청 본문 존재 여부 검사
  2. **JSON 읽기**: StreamUtils로 본문을 문자열로 변환
  3. **객체 변환**: ObjectMapper로 LogInDto 객체 변환
- **예외**: 
  - `InvalidLoginRequestException`: 빈 요청 본문
  - `IOException`: JSON 파싱 오류

### 5.3 `void validateLoginInput(String username, String password)`
- **역할**: 로그인 입력값 유효성 검증
- **접근성**: private
- **인자**:
  - `username` (String): 사용자명(이메일)
  - `password` (String): 비밀번호
- **반환값**: `void`
- **동작**: InputValidator로 이메일 형식 및 비밀번호 유효성 검증
- **예외**: `InvalidLoginRequestException` - 형식 오류 시

### 5.4 `void successfulAuthentication(...)`
- **역할**: 인증 성공 시 JWT 토큰 발급 및 응답 처리
- **어노테이션**: @Override
- **인자**:
  - `request`, `response`: HTTP 요청/응답
  - `chain`: 필터 체인 (사용하지 않음)
  - `authentication`: 인증 성공 정보
- **반환값**: `void`
- **동작**:
  1. **사용자 정보 추출**: CustomUserDetails에서 username, role 획득
  2. **토큰 생성**: Access Token, Refresh Token 생성
  3. **Redis 저장**: Refresh Token을 Redis에 저장
  4. **응답 설정**: 
     - Access Token은 응답 헤더에 설정
     - Refresh Token은 HttpOnly 쿠키로 설정
     - 200 OK 상태와 성공 메시지 전송

### 5.5 `void unsuccessfulAuthentication(...)`
- **역할**: 인증 실패 시 오류 응답 처리
- **어노테이션**: @Override
- **인자**:
  - `request`, `response`: HTTP 요청/응답
  - `failed`: 인증 실패 예외
- **반환값**: `void`
- **동작**:
  1. **로깅**: 실패한 IP 주소 기록
  2. **응답 설정**: 401 UNAUTHORIZED 상태와 실패 메시지 전송

### 5.6 `Cookie createCookie(String key, String value)`
- **역할**: HTTP 쿠키 생성
- **접근성**: private
- **인자**:
  - `key` (String): 쿠키 이름
  - `value` (String): 쿠키 값
- **반환값**: `Cookie` - 설정된 쿠키 객체
- **동작**:
  1. Cookie 객체 생성
  2. 경로("/"), 만료시간(1일), HttpOnly 플래그 설정
- **보안**: HttpOnly 설정으로 XSS 공격 방지

### 5.7 `void storeRefreshToken(String username, String refreshToken)`
- **역할**: Redis에 Refresh Token 저장
- **접근성**: private
- **인자**:
  - `username` (String): 사용자명(키 생성용)
  - `refreshToken` (String): 저장할 토큰
- **반환값**: `void`
- **동작**:
  1. Duration 객체 생성 (1일)
  2. Redis에 "refresh_사용자명" 키로 토큰 저장
- **예외**: `TechnicalException` - Redis 저장 실패 시

## 6. 인증 흐름

### 6.1 성공적인 로그인 흐름
1. **POST /login**: 클라이언트가 JSON으로 로그인 요청
2. **JSON 파싱**: LogInDto 객체로 변환
3. **입력값 검증**: 이메일/비밀번호 형식 확인
4. **DB 인증**: CustomUserDetailsService를 통한 사용자 검증
5. **토큰 생성**: Access/Refresh Token 쌍 생성
6. **토큰 저장**: Refresh Token을 Redis와 쿠키에 저장
7. **응답**: Access Token 헤더 + 성공 메시지

### 6.2 실패 시나리오
- **잘못된 JSON**: 400 형태의 InvalidLoginRequestException
- **형식 오류**: 이메일/비밀번호 형식 불일치
- **인증 실패**: 존재하지 않는 계정 또는 비밀번호 불일치
- **시스템 오류**: Redis 연결 실패 등

## 7. 보안 고려사항

### 7.1 토큰 보안
- **Access Token**: HTTP 헤더로 전송 (짧은 만료시간)
- **Refresh Token**: HttpOnly 쿠키 + Redis 저장 (긴 만료시간)
- **토큰 분리**: 각각의 역할과 위험도에 맞는 저장 방식

### 7.2 입력값 검증
- **이메일 형식**: InputValidator로 정규식 검증
- **비밀번호 강도**: 최소 길이 및 복잡성 검증
- **JSON 검증**: 올바른 JSON 형식 확인

## 8. 예외 처리 전략

### 8.1 비즈니스 예외
- `InvalidLoginRequestException`: 클라이언트 입력 오류
- 처리: 400 Bad Request로 클라이언트에게 피드백

### 8.2 시스템 예외
- `TechnicalException`: 시스템 레벨 오류 (Redis, DB 등)
- 처리: 500 Internal Server Error, 상세 정보 숨김

### 8.3 인증 예외
- `AuthenticationException`: Spring Security 인증 실패
- 처리: 401 Unauthorized, 재로그인 요청

## 9. 활용 시나리오

### 9.1 프론트엔드 로그인
```javascript
fetch('/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    emailId: 'user@example.com',
    password: 'password123'
  })
}).then(response => {
  const accessToken = response.headers.get('access');
  // Access Token을 로컬스토리지나 메모리에 저장
});
```

### 9.2 모바일 앱 로그인
```java
// 동일한 JSON API 사용
// Refresh Token은 쿠키 대신 응답 본문으로 전달 가능
```

## 10. 성능 최적화

### 10.1 Redis 활용
- **Refresh Token 관리**: 메모리 기반 빠른 액세스
- **만료 처리**: Redis TTL 자동 만료 활용

### 10.2 토큰 크기
- **최소한의 클레임**: username, role, category만 포함
- **압축**: JWT 헤더 최적화

## 11. 확장 가능성
- **소셜 로그인**: OAuth 플로우 통합
- **다중 기기**: 기기별 Refresh Token 관리
- **보안 강화**: 로그인 시도 제한, CAPTCHA 연동
- **감사 로깅**: 로그인 이력 추적
- **토큰 관리**: Refresh Token 로테이션
