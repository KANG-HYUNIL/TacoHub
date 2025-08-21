# AccountService

**패키지:** com.example.TacoHub.Service

## 1. 개요
`AccountService`는 사용자 계정 관련 비즈니스 로직을 처리하는 서비스 클래스입니다. 회원가입, 이메일 중복 검사, 인증코드 검증 등 계정 관리의 핵심 기능을 담당하며, Spring Security와 연동하여 안전한 사용자 관리를 제공합니다.

## 2. 의존성 및 환경
- **Spring Framework**: @Service, @RequiredArgsConstructor
- **Spring Security**: BCryptPasswordEncoder (비밀번호 암호화)
- **TacoHub Components**: AuthCodeService, AccountRepository, AccountConverter
- **TacoHub DTOs**: AccountDto, EmailVerificationDto
- **TacoHub Entities**: AccountEntity
- **TacoHub Exceptions**: EmailAlreadyExistsException, InvalidAuthCodeException
- **Lombok**: @RequiredArgsConstructor, @Slf4j

## 3. 클래스 멤버 및 의미

### 3.1 서비스 의존성
#### `authCodeService` (AuthCodeService, final)
- **의미**: 이메일 인증코드 생성/검증 서비스
- **역할**: 회원가입 시 이메일 인증코드 검증 담당
- **주입**: 생성자 주입(final)

#### `accountRepository` (AccountRepository, final)
- **의미**: 계정 데이터 접근 레포지토리
- **역할**: 계정 저장, 중복 검사 등 데이터베이스 연동
- **주입**: 생성자 주입(final)

#### `passwordEncoder` (BCryptPasswordEncoder, final)
- **의미**: 비밀번호 암호화 인코더
- **역할**: 사용자 비밀번호를 BCrypt 알고리즘으로 암호화
- **주입**: 생성자 주입(final)

### 3.2 상수
#### `ROLE_USER` (String, private final)
- **값**: "ROLE_USER"
- **의미**: 일반 사용자 기본 권한
- **활용**: 회원가입 시 기본 권한 설정

#### `ROLE_ADMIN` (String, private final)
- **값**: "ROLE_ADMIN"
- **의미**: 관리자 권한 (현재 미사용, 확장성 확보)
- **활용**: 향후 관리자 계정 생성 시 활용 예정

## 4. 메서드 상세 설명

### 4.1 `boolean checkEmailId(String emailId)`
- **역할**: 이메일 ID 중복 검사를 수행
- **인자**: 
  - `emailId` (String): 중복 검사할 이메일 ID
- **반환값**: `boolean`
  - `true`: 이메일이 이미 존재함 (중복)
  - `false`: 이메일이 존재하지 않음 (사용 가능)
- **동작**:
  1. AccountRepository의 existsByEmailId() 메서드 호출
  2. 데이터베이스에서 이메일 존재 여부 확인
  3. 결과를 boolean으로 반환
- **예외**: 데이터베이스 연결 오류 시 DataAccessException 발생 가능
- **활용**: 회원가입 전 이메일 중복 검사, 실시간 검증

### 4.2 `void signUp(AccountDto accountDto, String authCode, String purpose)`
- **역할**: 사용자 회원가입 처리 (인증코드 검증 포함)
- **인자**:
  - `accountDto` (AccountDto): 회원가입 정보 (이메일, 비밀번호, 이름 등)
  - `authCode` (String): 이메일 인증 코드
  - `purpose` (String): 인증 목적 (예: "회원가입")
- **반환값**: `void` (성공 시 정상 완료, 실패 시 예외 발생)
- **동작**:
  1. **이메일 중복 재검사**: checkEmailId()로 이메일 중복 확인
  2. **인증코드 검증**: AuthCodeService.verifyAuthCode()로 인증코드 유효성 확인
  3. **기본 권한 설정**: accountDto.setRole(ROLE_USER)로 일반 사용자 권한 부여
  4. **비밀번호 암호화**: BCryptPasswordEncoder로 평문 비밀번호 암호화
  5. **엔티티 변환**: AccountConverter.toEntity()로 DTO → Entity 변환
  6. **데이터베이스 저장**: AccountRepository.save()로 계정 정보 영구 저장
- **예외**:
  - `EmailAlreadyExistsException`: 이미 존재하는 이메일인 경우
  - `InvalidAuthCodeException`: 인증 코드가 유효하지 않은 경우
  - `DataAccessException`: 데이터베이스 저장 실패 시
- **보안 고려사항**:
  - 클라이언트 중복 검사를 믿지 않고 서버에서 재검증
  - 비밀번호는 BCrypt로 단방향 암호화
  - 인증코드 검증을 통한 이메일 소유권 확인

## 5. 동작 흐름

### 5.1 회원가입 전체 흐름
1. **클라이언트**: 이메일 중복 검사 요청 (`checkEmailId`)
2. **클라이언트**: 이메일 인증코드 발송 요청
3. **클라이언트**: 인증코드 입력 후 회원가입 요청 (`signUp`)
4. **서버**: 이메일 중복 재검사 (보안)
5. **서버**: 인증코드 검증
6. **서버**: 비밀번호 암호화 및 계정 저장

### 5.2 보안 검증 체계
- **이중 검증**: 클라이언트/서버 양쪽에서 이메일 중복 검사
- **인증코드**: Redis 기반 시간 제한 인증코드 검증
- **비밀번호 보안**: BCrypt 암호화로 평문 저장 방지

## 6. 예외 처리 전략

### 6.1 `EmailAlreadyExistsException`
- **발생 시점**: 회원가입 시 이미 존재하는 이메일 사용
- **처리 방법**: 사용자에게 다른 이메일 사용 요청
- **로깅**: 중복 시도 빈도 모니터링

### 6.2 `InvalidAuthCodeException`
- **발생 시점**: 잘못된 인증코드 입력 또는 만료된 코드 사용
- **처리 방법**: 새로운 인증코드 재발송 유도
- **보안**: 과도한 재시도 방지 로직 필요

## 7. 활용 시나리오

### 7.1 일반 회원가입
```java
// 이메일 중복 검사
if (accountService.checkEmailId("user@example.com")) {
    throw new EmailAlreadyExistsException("이미 사용 중인 이메일입니다.");
}

// 회원가입 처리
AccountDto dto = new AccountDto("user@example.com", "password123", "홍길동", null);
accountService.signUp(dto, "123456", "회원가입");
```

### 7.2 관리자 계정 생성 (확장)
```java
// 향후 관리자 계정 생성 시
AccountDto adminDto = new AccountDto("admin@example.com", "adminpass", "관리자", "ROLE_ADMIN");
// 별도 메서드로 관리자 계정 생성 로직 구현 예정
```

## 8. 보안 및 성능 고려사항

### 8.1 보안
- **비밀번호 정책**: 충분한 길이와 복잡성 요구 (클라이언트 검증)
- **인증코드 보안**: Redis TTL 기반 시간 제한, 재사용 방지
- **SQL 인젝션 방지**: JPA Repository 사용으로 자동 방어

### 8.2 성능
- **중복 검사 최적화**: 이메일 인덱스 활용
- **비밀번호 암호화**: BCrypt 강도 조절로 성능 최적화
- **트랜잭션**: 필요시 @Transactional 적용

## 9. 확장 가능성
- **소셜 로그인**: OAuth 기반 소셜 계정 연동
- **다중 권한**: 단일 권한에서 다중 권한 시스템 확장
- **계정 상태 관리**: 활성/비활성/휴면 상태 관리
- **비밀번호 정책**: 복잡성 검증, 주기적 변경 등
- **AuthCodeService**: 인증 코드 검증
- **PasswordEncoder**: 비밀번호 암호화

### 협력 클래스
- **AccountConverter**: Entity ↔ DTO 변환
- **EmailService**: 이메일 발송 (간접적)

## 관련 클래스

- **Entity**: [AccountEntity](../Entity/AccountEntity.md)
- **DTO**: [AccountDto](../Dto/AccountDto.md)
- **Repository**: [AccountRepository](../Repository/AccountRepository.md)
- **Controller**: [AccountController](../Controller/AccountController.md)

## 비즈니스 로직

### 회원가입 프로세스
1. 이메일 중복 확인
2. 인증 코드 검증
3. 비밀번호 암호화
4. 계정 정보 저장
5. 인증 코드 삭제

### 보안 고려사항
- 비밀번호는 BCrypt로 암호화
- 인증 코드 검증 후 즉시 삭제
- 이메일 중복 확인 필수

## 트랜잭션 관리
- `@Transactional`: 회원가입 과정의 원자성 보장
- 인증 코드 삭제는 별도 트랜잭션으로 분리

## 예외 처리
- 중복 이메일 검증
- 인증 코드 유효성 확인
- 데이터베이스 제약 조건 위반 처리
