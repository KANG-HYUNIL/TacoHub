# AccountEntity

**경로:** `com.example.TacoHub.Entity.AccountEntity`

## 개요

사용자 계정 정보를 담당하는 엔티티 클래스입니다. 사용자의 이메일 ID, 비밀번호, 이름, 권한 정보를 저장하며, 데이터베이스의 account 테이블과 매핑됩니다.

## 주요 속성

- **`emailId`**: 이메일 ID (기본 키)
  - 타입: String
  - 제약조건: NOT NULL, UNIQUE
  - 설명: 사용자를 식별하는 고유한 이메일 주소

- **`password`**: 암호화된 비밀번호
  - 타입: String
  - 제약조건: NOT NULL
  - 설명: BCrypt로 암호화된 사용자 비밀번호

- **`name`**: 사용자 이름
  - 타입: String
  - 제약조건: NOT NULL
  - 설명: 사용자의 실제 이름

- **`role`**: 권한
  - 타입: String
  - 가능한 값: "ROLE_USER", "ROLE_LEADER"
  - 설명: 사용자의 시스템 내 권한 레벨

## 관련 클래스

- **DTO**: [AccountDto](../Dto/AccountDto.md)
- **Repository**: [AccountRepository](../Repository/AccountRepository.md)
- **Service**: [AccountService](../Service/AccountService.md)
- **Controller**: [AccountController](../Controller/AccountController.md)
- **Converter**: [AccountConverter](../Converter/AccountConverter.md)

## 상속 관계

- **부모 클래스**: [BaseDateEntity](BaseDateEntity.md) - 생성일시, 수정일시 자동 관리

## 데이터베이스 스키마

```sql
CREATE TABLE account (
    email_id VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 주요 기능

1. **사용자 인증**: 로그인 시 이메일과 비밀번호를 통한 사용자 인증
2. **권한 관리**: 역할 기반 접근 제어(RBAC)를 위한 권한 정보 저장
3. **감사 추적**: BaseDateEntity 상속을 통한 생성/수정 시점 자동 기록

## 보안 고려사항

- 비밀번호는 반드시 BCrypt로 암호화되어 저장
- 이메일 중복 확인 로직 필수
- 개인정보 처리 시 로깅에서 민감정보 마스킹 적용
