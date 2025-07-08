# AccountController

**경로:** `com.example.TacoHub.Controller.AccountController`

## 개요

사용자 계정 관련 API 요청을 처리하는 REST 컨트롤러입니다. 클라이언트로부터 계정 관련 HTTP 요청을 받아 비즈니스 로직을 처리하고 적절한 HTTP 응답을 반환합니다.

## 주요 엔드포인트

### 1. POST /account/postSignup/{authCode}/{purpose}
**설명**: 사용자 회원가입 처리
- **HTTP 메서드**: POST
- **경로 변수**: 
  - `authCode`: 이메일 인증 코드
  - `purpose`: 인증 목적 (회원가입)
- **요청 본문**: `AccountDto` (JSON)
- **응답**: 회원가입 성공/실패 메시지

#### 요청 예시
```http
POST /account/postSignup/123456/signup
Content-Type: application/json

{
  "emailId": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "role": "ROLE_USER"
}
```

#### 응답 예시
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": null
}
```

### 2. POST /account/postCheckEmailId
**설명**: 이메일 ID 중복 확인
- **HTTP 메서드**: POST
- **요청 본문**: 이메일 주소 (String)
- **응답**: 중복 여부 정보

#### 요청 예시
```http
POST /account/postCheckEmailId
Content-Type: application/json

{
  "emailId": "user@example.com"
}
```

#### 응답 예시
```json
{
  "success": true,
  "message": "사용 가능한 이메일입니다.",
  "available": true
}
```

## 의존성

### 주입받는 서비스
- **AccountService**: 계정 관련 비즈니스 로직 처리

## 관련 클래스

- **Service**: [AccountService](../Service/AccountService.md)
- **DTO**: [AccountDto](../Dto/AccountDto.md)
- **Entity**: [AccountEntity](../Entity/AccountEntity.md)

## HTTP 상태 코드

### 성공 응답
- **200 OK**: 요청 성공
- **201 Created**: 회원가입 성공

### 오류 응답
- **400 Bad Request**: 잘못된 요청 데이터
- **409 Conflict**: 이메일 중복
- **422 Unprocessable Entity**: 인증 코드 불일치
- **500 Internal Server Error**: 서버 내부 오류

## 예외 처리

컨트롤러에서 발생하는 예외는 [GlobalExceptionHandler](../Exception/GlobalExceptionHandler.md)에서 전역적으로 처리됩니다:

- `EmailAlreadyExistsException` → 409 Conflict
- `InvalidAuthCodeException` → 422 Unprocessable Entity
- `InvalidLoginRequestException` → 400 Bad Request

## 보안 고려사항

- **입력 유효성 검증**: `@Valid` 어노테이션 활용
- **XSS 방지**: JSON 응답 시 HTML 이스케이핑
- **CSRF 보호**: Spring Security 기본 CSRF 토큰 검증
- **비밀번호 노출 방지**: 응답에 비밀번호 포함하지 않음

## API 문서화

자세한 API 명세는 [Api.md](../../Api.md)를 참조하세요.
