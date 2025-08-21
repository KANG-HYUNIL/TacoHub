# JwtFilter

**패키지:** com.example.TacoHub.Utils.Jwt

## 1. 개요
`JwtFilter`는 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 Spring Security 인증 컨텍스트를 설정하는 필터입니다. `OncePerRequestFilter`를 상속하여 요청당 한 번만 실행되며, JWT 기반 인증 시스템의 핵심 컴포넌트입니다.

## 2. 의존성 및 환경
- **Spring Security**: OncePerRequestFilter, SecurityContextHolder, Authentication
- **Spring Framework**: FilterChain, HttpServletRequest, HttpServletResponse
- **TacoHub JWT**: JwtUtil, CustomUserDetails
- **TacoHub Entity**: AccountEntity
- **Servlet API**: jakarta.servlet
- **Lombok**: @Slf4j

## 3. 클래스 멤버 및 의미

### 3.1 JWT 유틸리티
#### `jwtUtil` (JwtUtil, final)
- **의미**: JWT 토큰 생성/검증/파싱 유틸리티
- **역할**: 토큰 만료 확인, 카테고리 검증, 사용자 정보 추출
- **주입**: 생성자 주입

### 3.2 상수 정의
#### `ACCESS_TOKEN_HEADER` (String, static final)
- **값**: "access"
- **의미**: HTTP 헤더에서 Access Token을 찾기 위한 헤더명
- **용도**: request.getHeader("access")로 토큰 추출

#### `ACCESS_CATEGORY` (String, static final)
- **값**: "access"
- **의미**: 유효한 Access Token의 카테고리 값
- **용도**: Refresh Token과 구분하기 위한 검증

## 4. 생성자
### 4.1 `JwtFilter(JwtUtil jwtUtil)`
- **역할**: JwtFilter 객체 초기화
- **인자**: `jwtUtil` (JwtUtil): JWT 처리 유틸리티
- **동작**: jwtUtil 멤버 변수 초기화

## 5. 메서드 상세 설명

### 5.1 `void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`
- **역할**: 모든 HTTP 요청에 대한 JWT 토큰 검증 및 인증 처리
- **어노테이션**: @Override (OncePerRequestFilter 구현)
- **인자**:
  - `request` (HttpServletRequest): HTTP 요청 객체
  - `response` (HttpServletResponse): HTTP 응답 객체
  - `filterChain` (FilterChain): 다음 필터 체인
- **반환값**: `void`
- **동작**:
  1. **토큰 추출**: request.getHeader("access")로 Access Token 획득
  2. **토큰 존재 확인**: 토큰이 없으면 다음 필터로 진행 (익명 사용자)
  3. **만료 검증**: jwtUtil.isExpired()로 토큰 만료 확인
  4. **카테고리 검증**: jwtUtil.getCategory()로 "access" 토큰인지 확인
  5. **인증 객체 생성**: createAuthenticationFromToken()으로 Authentication 생성
  6. **SecurityContext 설정**: SecurityContextHolder에 인증 정보 등록
  7. **다음 필터 진행**: filterChain.doFilter() 호출
- **예외**:
  - 토큰 만료 시: 406 NOT_ACCEPTABLE 응답
  - 잘못된 토큰 카테고리: 401 UNAUTHORIZED 응답
  - IOException, ServletException: 필터 체인 처리 오류

### 5.2 `void sendErrorResponse(HttpServletResponse response, String message, int status)`
- **역할**: 클라이언트에게 오류 응답 전송
- **접근성**: private (내부 헬퍼 메서드)
- **인자**:
  - `response` (HttpServletResponse): 응답 객체
  - `message` (String): 오류 메시지
  - `status` (int): HTTP 상태 코드
- **반환값**: `void`
- **동작**:
  1. PrintWriter로 응답 본문에 메시지 작성
  2. HTTP 상태 코드 설정
- **예외**: `IOException` - 응답 작성 실패 시

### 5.3 `Authentication createAuthenticationFromToken(String token)`
- **역할**: JWT 토큰에서 Spring Security Authentication 객체 생성
- **접근성**: private (내부 헬퍼 메서드)
- **인자**: `token` (String): 유효한 JWT Access Token
- **반환값**: `Authentication` - Spring Security 인증 객체
- **동작**:
  1. **사용자 정보 추출**: jwtUtil.getUsername(), getRole()로 토큰에서 정보 획득
  2. **AccountEntity 생성**: 임시 AccountEntity 객체 생성 및 정보 설정
  3. **CustomUserDetails 생성**: AccountEntity 기반 UserDetails 구현체 생성
  4. **Authentication 생성**: UsernamePasswordAuthenticationToken 생성
- **특징**: 
  - DB 조회 없이 JWT 토큰 정보만으로 인증 객체 생성
  - 성능 최적화를 위한 토큰 기반 인증

## 6. 필터 동작 흐름

### 6.1 정상 인증 흐름
1. **요청 수신**: HTTP 요청이 JwtFilter에 도달
2. **토큰 추출**: "access" 헤더에서 JWT 토큰 획득
3. **토큰 검증**: 만료, 카테고리, 서명 검증
4. **인증 설정**: SecurityContext에 인증 정보 저장
5. **다음 필터**: 인증된 상태로 다음 필터 체인 진행

### 6.2 인증 실패 흐름
1. **토큰 없음**: 익명 사용자로 다음 필터 진행
2. **토큰 만료**: 406 상태로 재로그인 요청
3. **잘못된 토큰**: 401 상태로 인증 실패 응답

## 7. 보안 고려사항

### 7.1 토큰 검증 단계
- **서명 검증**: JwtUtil에서 SecretKey로 서명 무결성 확인
- **만료 시간 검증**: 토큰 exp 클레임 확인
- **카테고리 검증**: Access/Refresh 토큰 구분

### 7.2 성능 최적화
- **DB 조회 생략**: 토큰 정보만으로 인증 처리
- **요청당 1회 실행**: OncePerRequestFilter로 중복 실행 방지
- **조기 종료**: 토큰 없으면 검증 과정 생략

## 8. 예외 처리 전략

### 8.1 토큰 관련 예외
- **ExpiredJwtException**: jwtUtil.isExpired()에서 내부 처리
- **MalformedJwtException**: 잘못된 토큰 형식
- **SignatureException**: 서명 검증 실패

### 8.2 HTTP 응답 코드
- **200**: 정상 인증 후 다음 필터 진행
- **401**: 잘못된 토큰 (UNAUTHORIZED)
- **406**: 만료된 토큰 (NOT_ACCEPTABLE)

## 9. 활용 시나리오

### 9.1 API 요청 인증
```http
GET /api/workspace
Headers:
  access: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 9.2 익명 접근
```http
GET /api/public
Headers: (access 헤더 없음)
```

### 9.3 토큰 만료 처리
```http
Response:
  Status: 406 Not Acceptable
  Body: "access token expired"
```

## 10. 필터 체인에서의 위치
- **위치**: LoginFilter 다음, 비즈니스 로직 필터 이전
- **순서**: Spring Security FilterChain에서 인증 필터 위치
- **역할**: 모든 요청에 대한 JWT 기반 인증 게이트웨이

## 11. 확장 가능성
- **토큰 블랙리스트**: 로그아웃된 토큰 검증
- **권한 기반 접근 제어**: URL별 권한 검사
- **토큰 갱신**: 만료 임박 시 자동 갱신
- **사용자 상태 검증**: 계정 비활성화 상태 확인
- **로깅 강화**: 인증 시도, 실패 등 보안 로그

## 12. 성능 모니터링
- **인증 성공/실패율**: 토큰 검증 통계
- **응답 시간**: 필터 처리 시간 측정
- **오류 빈도**: 만료/잘못된 토큰 빈도 모니터링
