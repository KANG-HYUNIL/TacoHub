# TacoHub 백엔드 클래스 구조

## 목차

1. [엔티티(Entity)](#엔티티)
2. [DTO(Data Transfer Object)](#dto)
3. [컨트롤러(Controller)](#컨트롤러)
4. [서비스(Service)](#서비스)
5. [레포지토리(Repository)](#레포지토리)
6. [컨버터(Converter)](#컨버터)
7. [예외(Exception)](#예외)
8. [설정(Config)](#설정)
9. [JWT 및 인증(Authentication)](#jwt-및-인증)

---

## 엔티티

엔티티 클래스는 데이터베이스 테이블과 매핑되는 객체입니다.

### AccountEntity

**경로:** `com.example.TacoHub.Entity.AccountEntity`

**설명:** 사용자 계정 정보를 담당하는 엔티티입니다. 사용자의 이메일 ID, 비밀번호, 이름, 권한 정보를 저장합니다.

**주요 속성:**
- `emailId`: 이메일 ID (기본 키)
- `password`: 암호화된 비밀번호
- `name`: 사용자 이름
- `role`: 권한 (ROLE_USER, ROLE_LEADER)

---

## DTO

DTO 클래스는 계층 간 데이터 전송을 위한 객체입니다.

### AccountDto

**경로:** `com.example.TacoHub.Dto.AccountDto`

**설명:** 사용자 계정 정보를 전달하는 DTO입니다. 계정 생성, 조회, 수정 등에 사용됩니다.

**주요 속성:**
- `emailId`: 이메일 ID
- `password`: 비밀번호
- `name`: 사용자 이름
- `role`: 권한

### EmailVerificationDto

**경로:** `com.example.TacoHub.Dto.EmailVerificationDto`

**설명:** 이메일 인증 관련 정보를 전달하는 DTO입니다. 인증 코드 생성, 검증에 사용됩니다.

**주요 속성:**
- `email`: 이메일 주소
- `authCode`: 인증 코드
- `purpose`: 인증 목적 (회원가입, 비밀번호 재설정 등)

### LogInDto

**경로:** `com.example.TacoHub.Dto.LogInDto`

**설명:** 로그인 요청 시 사용자 로그인 정보를 전달하는 DTO입니다.

**주요 속성:**
- `emailId`: 이메일 ID
- `password`: 비밀번호

---

## 컨트롤러

컨트롤러는 클라이언트의 요청을 처리하고 응답을 반환합니다.

### AccountController

**경로:** `com.example.TacoHub.Controller.AccountController`

**설명:** 사용자 계정 관련 API 요청을 처리합니다. 회원가입, 이메일 중복 확인 등의 기능을 제공합니다.

**주요 엔드포인트:**
- `POST /account/postSignup/{authCode}/{purpose}`: 회원가입 처리
- `POST /account/postCheckEmailId`: 이메일 ID 중복 확인

### EmailController

**경로:** `com.example.TacoHub.Controller.EmailController`

**설명:** 이메일 인증 관련 API 요청을 처리합니다. 인증 코드 발송, 인증 코드 검증 기능을 제공합니다.

**주요 엔드포인트:**
- `POST /email/verification`: 인증 코드 발송
- `POST /email/verify`: 인증 코드 검증

### JwtController

**경로:** `com.example.TacoHub.Controller.JwtController`

**설명:** JWT 토큰 관련 API 요청을 처리합니다. 토큰 재발급 등의 기능을 제공할 예정입니다.

---

## 서비스

서비스 클래스는 비즈니스 로직을 처리합니다.

### AccountService

**경로:** `com.example.TacoHub.Service.AccountService`

**설명:** 사용자 계정 관련 비즈니스 로직을 처리합니다. 회원가입, 이메일 중복 확인 등의 기능을 구현합니다.

**주요 메서드:**
- `signUp(AccountDto, String, String)`: 회원가입 처리
- `checkEmailId(String)`: 이메일 ID 중복 확인

### AuthCodeService

**경로:** `com.example.TacoHub.Service.AuthCodeService`

**설명:** 인증 코드 생성, 검증, 저장 및 삭제 기능을 제공합니다. Redis와 연동하여 인증 코드의 유효성을 관리합니다.

**주요 메서드:**
- `createAuthCode()`: 6자리 인증 코드 생성
- `verifyAuthCode(EmailVerificationDto)`: 인증 코드 검증
- `setAuthCodeInRedis(String, String, String)`: Redis에 인증 코드 저장
- `deleteAuthCodeInRedis(String, String)`: Redis에서 인증 코드 삭제

### EmailService

**경로:** `com.example.TacoHub.Service.EmailService`

**설명:** 이메일 전송 기능을 제공합니다. 인증 코드 이메일 발송을 담당합니다.

**주요 메서드:**
- `sendAuthCodeToEmail(String, String)`: 인증 코드를 이메일로 전송

### RedisService

**경로:** `com.example.TacoHub.Service.RedisService`

**설명:** Redis 작업을 위한 서비스 클래스로, 인증 코드, 출석 체크 등의 임시 데이터를 저장하고 관리합니다.

**주요 메서드:**
- `setValues(String, T, Duration)`: Redis에 키-값 쌍 저장
- `getValues(String)`: Redis에서 값 조회
- `checkExistsValue(String)`: 키 존재 여부 확인
- `deleteValues(String)`: 키-값 쌍 삭제

---

## 레포지토리

레포지토리는 데이터 접근 계층을 담당합니다.

### AccountRepository

**경로:** `com.example.TacoHub.Repository.AccountRepository`

**설명:** 사용자 계정 정보에 대한 데이터 액세스를 제공합니다. JpaRepository를 상속받아 기본 CRUD 기능 및 추가 쿼리 메서드를 제공합니다.

**주요 메서드:**
- `existsByEmailId(String)`: 이메일 ID 존재 여부 확인
- `findByEmailIdContaining(String)`: 이메일 ID 포함 검색
- `findByNameContaining(String)`: 이름 포함 검색
- `findByEmailId(String)`: 이메일 ID 정확히 일치하는 계정 조회

---

## 컨버터

컨버터는 엔티티와 DTO 간의 변환을 담당합니다.

### AccountConverter

**경로:** `com.example.TacoHub.Converter.AccountConverter`

**설명:** AccountEntity와 AccountDto 간의 변환을 담당하는 컨버터 클래스입니다.

**주요 메서드:**
- `toEntity(AccountDto)`: DTO를 엔티티로 변환
- `toDTO(AccountEntity)`: 엔티티를 DTO로 변환

---

## 예외

예외 클래스는 애플리케이션의 예외 상황을 처리합니다.

### EmailAlreadyExistsException

**경로:** `com.example.TacoHub.Exception.EmailAlreadyExistsException`

**설명:** 이미 존재하는 이메일로 회원가입을 시도할 때 발생하는 예외입니다.

### InvalidAuthCodeException

**경로:** `com.example.TacoHub.Exception.InvalidAuthCodeException`

**설명:** 유효하지 않은 인증 코드로 인증을 시도할 때 발생하는 예외입니다.

### InvalidLoginRequestException

**경로:** `com.example.TacoHub.Exception.InvalidLoginRequestException`

**설명:** 로그인 요청이 유효하지 않을 때 발생하는 예외입니다. 이메일 또는 비밀번호 형식이 올바르지 않거나 요청 본문이 비어있는 경우 발생합니다.

### TechnicalException

**경로:** `com.example.TacoHub.Exception.TechnicalException`

**설명:** 시스템 내부 오류, 기술적 문제 발생 시 사용되는 예외입니다.

### GlobalExceptionHandler

**경로:** `com.example.TacoHub.Exception.GlobalExceptionHandler`

**설명:** 애플리케이션 전역의 예외를 처리하는 클래스입니다. @ControllerAdvice를 사용하여 다양한 예외에 대한 처리 로직을 구현합니다.

**주요 메서드:**
- `handleGlobalException(Exception)`: 일반 예외 처리
- `handleEmailAlreadyExistsException(EmailAlreadyExistsException)`: 이메일 중복 예외 처리
- `handleTechnicalException(TechnicalException)`: 기술적 예외 처리
- `handleInvalidAuthCodeException(InvalidAuthCodeException)`: 인증 코드 예외 처리
- `handleInvalidLoginRequestException(InvalidLoginRequestException)`: 잘못된 로그인 요청 예외 처리

---

## 설정

설정 클래스는 애플리케이션의 환경 설정을 담당합니다.

### EmailConfig

**경로:** `com.example.TacoHub.Config.EmailConfig`

**설명:** 이메일 전송 관련 설정을 담당합니다. JavaMailSender를 설정하고 관리합니다.

**주요 메서드:**
- `javaMailSender()`: JavaMailSender 빈 생성 및 설정
- `getMailProperties()`: 이메일 전송에 필요한 속성 설정

### RedisConfig

**경로:** `com.example.TacoHub.Config.RedisConfig`

**설명:** Redis 연결 및 RedisTemplate 설정을 담당합니다.

**주요 메서드:**
- `redisConnectionFactory()`: Redis 연결 팩토리 생성 및 설정
- `redisTemplate()`: Redis 작업을 위한 템플릿 생성 및 설정

### SecurityConfig

**경로:** `com.example.TacoHub.Config.SecurityConfig`

**설명:** Spring Security 관련 설정을 담당합니다. 인증/인가 규칙, 필터 체인 구성, 비밀번호 인코더 등을 설정합니다.

**주요 메서드:**
- `authenticationManager(AuthenticationConfiguration)`: 인증 관리자 빈 등록
- `webSecurityCustomizer()`: Spring Security 적용 제외 경로 설정
- `passwordEncoder()`: 비밀번호 암호화 인코더 빈 등록
- `filterChain(HttpSecurity)`: 보안 필터 체인 구성 및 인가 규칙 설정

---

## JWT 및 인증

JWT 및 인증 관련 클래스들은 보안 인증 및 인가 기능을 담당합니다.

### JwtUtil

**경로:** `com.example.TacoHub.Utils.Jwt.JwtUtil`

**설명:** JWT 토큰의 생성 및 검증을 담당하는 유틸리티 클래스입니다.

**주요 메서드:**
- `getCategory(String)`: 토큰의 카테고리(access/refresh) 확인
- `isExpired(String)`: 토큰의 만료 여부 확인
- `getUsername(String)`: 토큰에서 사용자명(이메일) 추출
- `getRole(String)`: 토큰에서 역할(권한) 정보 추출
- `createAccessJwt(String, String)`: Access Token 생성
- `createRefreshJwt(String, String)`: Refresh Token 생성

### JwtFilter

**경로:** `com.example.TacoHub.Utils.Jwt.JwtFilter`

**설명:** 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검증하는 필터입니다.

**주요 메서드:**
- `doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`: 요청에서 토큰을 추출하고 검증
- `sendErrorResponse(HttpServletResponse, String, int)`: 오류 응답 전송
- `createAuthenticationFromToken(String)`: JWT 토큰에서 인증 객체 생성

### LoginFilter

**경로:** `com.example.TacoHub.Utils.Jwt.LoginFilter`

**설명:** 사용자 로그인 요청을 처리하는 필터로, 인증 성공 시 JWT 토큰을 발급합니다.

**주요 메서드:**
- `attemptAuthentication(HttpServletRequest, HttpServletResponse)`: 로그인 시도 및 인증 처리
- `successfulAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, Authentication)`: 인증 성공 시 처리
- `unsuccessfulAuthentication(HttpServletRequest, HttpServletResponse, AuthenticationException)`: 인증 실패 시 처리
- `extractLoginDtoFromRequest(HttpServletRequest)`: 요청에서 로그인 정보 추출
- `validateLoginInput(String, String)`: 로그인 입력값 유효성 검증
- `createCookie(String, String)`: HTTP 쿠키 생성
- `storeRefreshToken(String, String)`: Redis에 Refresh 토큰 저장

### CustomUserDetails

**경로:** `com.example.TacoHub.Utils.Jwt.CustomUserDetails`

**설명:** Spring Security의 UserDetails 인터페이스를 구현하여 인증에 필요한 사용자 정보를 제공합니다.

**주요 메서드:**
- `getAuthorities()`: 사용자 권한 정보 반환
- `getPassword()`: 사용자 비밀번호 반환
- `getUsername()`: 사용자명(이메일) 반환
- `isAccountNonExpired()`: 계정 만료 여부 확인
- `isAccountNonLocked()`: 계정 잠금 여부 확인
- `isCredentialsNonExpired()`: 자격 증명 만료 여부 확인
- `isEnabled()`: 계정 활성화 여부 확인

### CustomUserDetailsService

**경로:** `com.example.TacoHub.Utils.Jwt.CustomUserDetailsService`

**설명:** Spring Security의 UserDetailsService 인터페이스를 구현하여 사용자 정보를 데이터베이스에서 로드합니다.

**주요 메서드:**
- `loadUserByUsername(String)`: 사용자명(이메일)으로 사용자 정보를 조회하여 UserDetails 객체로 변환