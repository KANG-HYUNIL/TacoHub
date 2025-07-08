# GlobalExceptionHandler

**경로:** `com.example.TacoHub.Exception.GlobalExceptionHandler`

## 개요

애플리케이션 전역의 예외를 처리하는 클래스입니다. `@ControllerAdvice`를 사용하여 컨트롤러에서 발생하는 다양한 예외에 대한 통일된 처리 로직을 구현하고, 클라이언트에게 일관된 오류 응답을 제공합니다.

## 주요 메서드

### 1. handleEmailAlreadyExistsException(EmailAlreadyExistsException)
**설명**: 이메일 중복 예외 처리
- **HTTP 상태**: 409 Conflict
- **응답 메시지**: "이미 존재하는 이메일입니다."
- **발생 상황**: 회원가입 시 중복된 이메일 사용

### 2. handleInvalidAuthCodeException(InvalidAuthCodeException)
**설명**: 인증 코드 불일치 예외 처리
- **HTTP 상태**: 422 Unprocessable Entity
- **응답 메시지**: "인증 코드가 일치하지 않습니다."
- **발생 상황**: 이메일 인증 시 잘못된 코드 입력

### 3. handleInvalidLoginRequestException(InvalidLoginRequestException)
**설명**: 잘못된 로그인 요청 예외 처리
- **HTTP 상태**: 400 Bad Request
- **응답 메시지**: "로그인 요청이 올바르지 않습니다."
- **발생 상황**: 로그인 시 필수 필드 누락 또는 형식 오류

### 4. handleTechnicalException(TechnicalException)
**설명**: 기술적 예외 처리
- **HTTP 상태**: 500 Internal Server Error
- **응답 메시지**: "시스템 내부 오류가 발생했습니다."
- **발생 상황**: 데이터베이스 연결 오류, 외부 시스템 오류 등

### 5. handleGlobalException(Exception)
**설명**: 일반 예외 처리 (최후의 안전망)
- **HTTP 상태**: 500 Internal Server Error
- **응답 메시지**: "예상치 못한 오류가 발생했습니다."
- **발생 상황**: 위에서 처리되지 않은 모든 예외

## 예외 처리 구조

### 예외 계층 구조
```
Exception (최상위)
├── TechnicalException (기술적 오류)
├── EmailAlreadyExistsException (이메일 중복)
├── InvalidAuthCodeException (인증 코드 불일치)
├── InvalidLoginRequestException (로그인 요청 오류)
└── 기타 RuntimeException들
```

### 처리 우선순위
1. 구체적인 커스텀 예외 (EmailAlreadyExistsException 등)
2. 기술적 예외 (TechnicalException)
3. 일반 예외 (Exception) - 최후의 안전망

## 표준 오류 응답 형식

```json
{
  "success": false,
  "message": "오류 메시지",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-15T14:30:22.123Z",
  "path": "/api/account/signup"
}
```

## 관련 예외 클래스들

### 비즈니스 예외
- [EmailAlreadyExistsException](EmailAlreadyExistsException.md)
- [InvalidAuthCodeException](InvalidAuthCodeException.md)
- [InvalidLoginRequestException](InvalidLoginRequestException.md)

### 시스템 예외
- [TechnicalException](TechnicalException.md)

## 로깅 전략

### 로그 레벨별 기록
- **ERROR**: TechnicalException, 예상치 못한 Exception
- **WARN**: InvalidAuthCodeException, EmailAlreadyExistsException
- **INFO**: InvalidLoginRequestException (보안 목적)

### 민감정보 보호
- 사용자 비밀번호, 토큰 등은 로그에 기록하지 않음
- 스택 트레이스는 개발 환경에서만 상세 기록

## 보안 고려사항

### 정보 노출 방지
- 운영 환경에서는 상세한 오류 정보 숨김
- 개발 환경에서만 스택 트레이스 포함
- 시스템 내부 구조 정보 노출 방지

### 로그 기록
- 모든 예외는 적절한 로그 레벨로 기록
- 보안 관련 예외는 별도 감사 로그 생성
- 사용자 행동 패턴 추적을 위한 메타데이터 포함

## 확장성

### 새로운 예외 추가
```java
@ExceptionHandler(NewCustomException.class)
public ResponseEntity<ErrorResponse> handleNewCustomException(NewCustomException e) {
    // 새로운 예외 처리 로직
}
```

### 특정 컨트롤러 예외 처리
각 컨트롤러에서 특별한 예외 처리가 필요한 경우, 개별 컨트롤러에 `@ExceptionHandler` 추가 가능 (GlobalExceptionHandler보다 우선 처리됨)
