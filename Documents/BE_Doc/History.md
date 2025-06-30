# TacoHub 개발 히스토리

## 2025-06-30: Notion Copy 기능 기반 구조 구현

### 구현 기능 요약
- Notion과 유사한 협업 문서 관리 시스템의 기반 구조 구현
- 워크스페이스(Workspace), 페이지(Page), 워크스페이스 사용자(WorkspaceUser) 개념 도입
- 계층적 페이지 구조 및 사용자 권한 관리 시스템 설계
- 공통 날짜 관리를 위한 Base 클래스 구현

### 새로 작성된 클래스

#### 기반 클래스
- `BaseDateEntity`: 모든 엔티티의 공통 부모 클래스, JPA Auditing을 통한 생성일/수정일 자동 관리
- `BaseDateDTO`: 모든 DTO의 공통 부모 클래스, 생성일/수정일 정보 포함

#### 엔티티 (Entity)
- `PageEntity`: 페이지 정보 저장, 계층 구조 지원 (부모-자식 관계)
- `WorkSpaceEntity`: 워크스페이스 정보 저장
- `WorkSpaceUserEntity`: 워크스페이스와 사용자 간의 관계 및 권한 정보 저장

#### DTO (Data Transfer Object)
- `PageDTO`: 페이지 정보 전송용 객체, 계층 구조 및 워크스페이스 정보 포함
- `WorkSpaceDTO`: 워크스페이스 정보 전송용 객체, 관련 페이지 및 사용자 정보 포함
- `WorkSpaceUserDTO`: 워크스페이스 사용자 관계 정보 전송용 객체

#### 레포지토리 (Repository)
- `PageRepository`: 페이지 데이터 액세스 인터페이스
- `WorkSpaceRepository`: 워크스페이스 데이터 액세스 인터페이스
- `WorkSpaceUserRepository`: 워크스페이스 사용자 관계 데이터 액세스 인터페이스
- `BlockDocumentRepository`: 블록 도큐먼트 데이터 액세스 인터페이스

#### 컨버터 (Converter)
- `PageConverter`: PageEntity와 PageDTO 간 변환, 계층 구조 처리
- `WorkSpaceConverter`: WorkSpaceEntity와 WorkSpaceDTO 간 변환
- `WorkSpaceUserConverter`: WorkSpaceUserEntity와 WorkSpaceUserDTO 간 변환

#### 서비스 (Service)
- `PageService`: 페이지 관련 비즈니스 로직 처리 (뼈대만 구성)
- `WorkSpaceService`: 워크스페이스 관련 비즈니스 로직 처리 (뼈대만 구성)
- `WorkSpaceUserService`: 워크스페이스 사용자 관리 비즈니스 로직 처리 (뼈대만 구성)
- `BlockService`: 블록 관련 비즈니스 로직 처리 (뼈대만 구성)

#### 열거형 (Enum)
- `WorkSpaceRole`: 워크스페이스 내 사용자 역할 정의 (OWNER, ADMIN, MEMBER, GUEST)
- `MembershipStatus`: 워크스페이스 멤버십 상태 정의 (ACTIVE, INVITED, SUSPENDED)

### 주요 설계 특징

#### 1. 계층적 페이지 구조
- 페이지 간 부모-자식 관계 지원
- `isRoot` 플래그를 통한 루트 페이지 식별
- `orderIndex`를 통한 페이지 정렬 순서 관리

#### 2. 워크스페이스 기반 협업
- 다중 사용자가 공유하는 워크스페이스 개념
- 워크스페이스별 사용자 권한 관리 (OWNER, ADMIN, MEMBER, GUEST)
- 멤버십 상태 관리 (ACTIVE, INVITED, SUSPENDED)

#### 3. UUID 기반 식별자
- 모든 주요 엔티티에서 UUID 사용으로 확장성 확보
- 분산 환경에서의 고유성 보장

#### 4. JPA Auditing 활용
- `BaseDateEntity`를 통한 생성일/수정일 자동 관리
- `@CreatedDate`, `@LastModifiedDate` 어노테이션 활용

### 데이터베이스 구조
- **workspace**: 워크스페이스 기본 정보
- **page**: 페이지 정보 및 계층 구조
- **workspace_user**: 워크스페이스와 사용자 간의 관계 테이블

### 앞으로 추가될 내용
- Service 및 Repository 클래스의 비즈니스 로직 기능 구현
- Controller와 Service 연결 및 REST API 구현
- 생성한 Entity, Service, Repository, Controller 등에 대한 Test Code 작성
- 페이지 블록 시스템 구현 (텍스트, 이미지, 테이블 등)
- 실시간 협업 기능 (WebSocket 활용)
- 권한 기반 접근 제어 상세 구현

## 2025-05-03 (2): JWT 기반 로그인 기능 구현

### 구현 기능 요약
- JWT 토큰 기반 인증/인가 시스템 구현
- Spring Security Filter를 활용한 로그인 처리
- Redis를 활용한 Refresh Token 관리
- 요청 별 토큰 검증 및 사용자 인증

### 새로 작성된 클래스

#### JWT 관련 유틸리티
- `JwtUtil`: JWT 토큰 생성 및 검증 기능 제공
- `JwtFilter`: 모든 요청에 대한 JWT 토큰 검증 필터
- `LoginFilter`: 로그인 요청 처리 및 JWT 토큰 발급 필터
- `CustomUserDetails`: Spring Security를 위한 사용자 정보 제공 클래스
- `CustomUserDetailsService`: 데이터베이스에서 사용자 정보 로드 서비스

#### 예외 처리
- `InvalidLoginRequestException`: 로그인 요청 유효성 검증 실패 시 발생하는 예외
- GlobalExceptionHandler에 로그인 관련 예외 처리 추가

#### 설정
- `SecurityConfig`: Spring Security 설정 클래스 보완
  - 필터 체인 구성
  - 인증/인가 규칙 정의
  - 비밀번호 인코더 및 AuthenticationManager 설정

### 개선된 기능
- 코드 가독성 및 유지보수성 향상을 위한 리팩토링
- 모든 클래스에 상세한 주석 추가
- 예외 처리 로직 강화
- 상수 및 메서드 분리를 통한 코드 간결화

### 동작 설명
1. 사용자가 이메일과 비밀번호로 로그인 요청
2. LoginFilter가 요청을 가로채 이메일과 비밀번호 추출
3. 입력값 유효성 검증 후 AuthenticationManager에 인증 요청
4. CustomUserDetailsService가 데이터베이스에서 사용자 정보 조회
5. BCrypt로 암호화된 비밀번호 검증
6. 인증 성공 시 Access Token과 Refresh Token 생성
7. Access Token은 응답 헤더에, Refresh Token은 쿠키에 포함하여 반환
8. 이후 요청 시 클라이언트는 헤더에 Access Token을 포함하여 전송
9. JwtFilter가 모든 요청을 가로채 토큰 유효성 검증
10. 유효한 토큰의 경우 SecurityContext에 인증 정보 설정 후 요청 처리 허용

### 보안 고려사항
- Access Token은 짧은 유효 기간(10시간), Refresh Token은 긴 유효 기간(57시간) 설정
- 모든 비밀번호는 BCrypt 알고리즘으로 암호화 저장
- Refresh Token은 서버 측 Redis에 저장하여 관리
- 토큰에 사용자 권한 정보를 포함하여 인가 처리

### 앞으로 추가될 내용
- 토큰 갱신 기능 구현
- 로그아웃 시 토큰 무효화
- 권한 별 리소스 접근 제한 구체화
- 소셜 로그인 연동

## 2025-05-03 (1): 사용자 인증/인가 기능 구현

### 구현 기능 요약
- 사용자 계정 관리 기능 - 회원가입, 이메일 중복 확인
- 이메일 인증 기능 - 인증 코드 발송, 인증 코드 검증
- Redis를 활용한 인증 코드 관리

### 새로 작성된 클래스

#### 엔티티
- `AccountEntity`: 사용자 계정 정보 테이블과 매핑

#### DTO
- `AccountDto`: 사용자 계정 정보 전송 객체
- `EmailVerificationDto`: 이메일 인증 관련 정보 전송 객체

#### 컨트롤러
- `AccountController`: 계정 관련 API 엔드포인트 제공
  - 회원가입 처리
  - 이메일 중복 확인
- `EmailController`: 이메일 인증 관련 API 엔드포인트 제공
  - 인증 코드 발송
  - 인증 코드 검증

#### 서비스
- `AccountService`: 사용자 계정 관련 비즈니스 로직
- `AuthCodeService`: 인증 코드 생성, 검증, 저장, 삭제 기능
- `EmailService`: 이메일 전송 기능
- `RedisService`: Redis 작업 관련 기능

#### 레포지토리
- `AccountRepository`: 사용자 계정 데이터 액세스

#### 컨버터
- `AccountConverter`: 엔티티와 DTO 간 변환 기능

#### 예외
- `EmailAlreadyExistsException`: 이메일 중복 예외
- `InvalidAuthCodeException`: 잘못된 인증 코드 예외
- `TechnicalException`: 시스템 내부 오류 예외
- `GlobalExceptionHandler`: 전역 예외 처리기

#### 설정
- `EmailConfig`: 이메일 전송 관련 설정
- `RedisConfig`: Redis 연결 및 설정
- `SecurityConfig`: Spring Security 기본 설정 (뼈대만 구성)

### 동작 설명
1. 사용자가 회원가입 화면에서 이메일 입력
2. 이메일 중복 확인 후 인증 코드 발송 요청
3. 서버는 6자리 랜덤 인증 코드 생성하여 이메일로 전송
4. 생성된 인증 코드를 Redis에 일정 시간(15분) 동안 저장
5. 사용자가 받은 인증 코드 입력 후 검증 요청
6. 검증 성공 시 회원 정보 입력하여 회원가입 완료
7. 비밀번호는 BCrypt로 암호화하여 데이터베이스에 저장

### 환경 설정
- Redis 연결 설정 구성
- 이메일 전송을 위한 SMTP 설정
- 인증 코드 만료 시간 설정 (15분)