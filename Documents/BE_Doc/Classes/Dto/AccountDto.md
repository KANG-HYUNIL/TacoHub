# AccountDto

**경로:** `com.example.TacoHub.Dto.AccountDto`

## 개요

사용자 계정 정보를 전달하는 DTO(Data Transfer Object) 클래스입니다. 계층 간 데이터 전송에 사용되며, 계정 생성, 조회, 수정 등의 API 요청/응답에 활용됩니다.

## 주요 속성

- **`emailId`**: 이메일 ID
  - 타입: String
  - 설명: 사용자를 식별하는 고유한 이메일 주소

- **`password`**: 비밀번호
  - 타입: String
  - 설명: 사용자 비밀번호 (평문, 서버에서 암호화 처리)

- **`name`**: 사용자 이름
  - 타입: String
  - 설명: 사용자의 실제 이름

- **`role`**: 권한
  - 타입: String
  - 가능한 값: "ROLE_USER", "ROLE_LEADER"
  - 설명: 사용자의 시스템 내 권한 레벨

## 관련 클래스

- **Entity**: [AccountEntity](../Entity/AccountEntity.md)
- **Repository**: [AccountRepository](../Repository/AccountRepository.md)
- **Service**: [AccountService](../Service/AccountService.md)
- **Controller**: [AccountController](../Controller/AccountController.md)
- **Converter**: [AccountConverter](../Converter/AccountConverter.md)

## 상속 관계

- **부모 클래스**: [BaseDateDTO](BaseDateDTO.md) - 생성일시, 수정일시 정보 포함

## 사용 시나리오

### 1. 회원가입
```json
{
  "emailId": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "role": "ROLE_USER"
}
```

### 2. 로그인 응답
```json
{
  "emailId": "user@example.com",
  "name": "홍길동",
  "role": "ROLE_USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 유효성 검증

- **이메일 형식**: 올바른 이메일 형식 검증 필요
- **비밀번호 강도**: 최소 8자, 영문/숫자/특수문자 포함 권장
- **이름 길이**: 최소 2자, 최대 50자
- **권한 값**: 정의된 권한 값만 허용

## 보안 고려사항

- 비밀번호는 로그에 기록되지 않도록 마스킹 처리
- 응답 시 비밀번호 필드 제외
- 민감정보는 HTTPS를 통해서만 전송
