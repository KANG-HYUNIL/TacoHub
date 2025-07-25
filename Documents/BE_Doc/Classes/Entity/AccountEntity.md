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
# AccountEntity

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Entity.AccountEntity</td></tr>
  <tr><th>클래스 설명</th><td>사용자 계정 정보를 담당하는 엔티티(Entity) 클래스.<br>이메일, 비밀번호, 이름, 권한 정보를 저장하며 DB의 account 테이블과 매핑된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>제약/예시</th></tr>
  <tr><td>emailId</td><td>String</td><td>사용자를 식별하는 고유 이메일 주소. 기본키.</td><td>NOT NULL, UNIQUE, "user@example.com"</td></tr>
  <tr><td>password</td><td>String</td><td>BCrypt로 암호화된 사용자 비밀번호.</td><td>NOT NULL</td></tr>
  <tr><td>name</td><td>String</td><td>사용자의 실제 이름.</td><td>NOT NULL, "홍길동"</td></tr>
  <tr><td>role</td><td>String</td><td>사용자의 시스템 내 권한 레벨.</td><td>"ROLE_USER", "ROLE_LEADER"</td></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>계정 생성 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>계정 수정 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>AccountEntity()</td><td>기본 생성자. JPA 및 Lombok에서 자동 생성.</td></tr>
  <tr><td>AccountEntity(emailId, password, name, role, createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Getter/@Setter로 자동 생성.</td>
    <td>각 필드별(String emailId, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>부모 클래스</th><th>설명</th></tr>
  <tr><td>BaseDateEntity</td><td>생성일시(createdAt), 수정일시(updatedAt) 자동 관리.</td></tr>
</table>

## 관련 클래스 (Relations)
<table>
  <tr><th>종류</th><th>클래스명</th><th>설명</th></tr>
  <tr><td>DTO</td><td>AccountDto</td><td>API 계층 데이터 전달용 DTO</td></tr>
  <tr><td>Repository</td><td>AccountRepository</td><td>DB 접근 레이어</td></tr>
  <tr><td>Service</td><td>AccountService</td><td>비즈니스 로직 처리</td></tr>
  <tr><td>Controller</td><td>AccountController</td><td>API 엔드포인트</td></tr>
  <tr><td>Converter</td><td>AccountConverter</td><td>Entity-DTO 변환</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. 회원가입/로그인/정보수정 등에서 AccountEntity 객체가 생성된다.
2. 각 필드에 값이 할당되어 DB에 저장된다.
3. Entity-DTO 변환, 비즈니스 로직 처리 등에 활용된다.

## DB 테이블 예시 (Schema)
```sql
CREATE TABLE account (
    email_id VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 예외 및 주의사항 (Exceptions & Notes)
- password는 반드시 암호화하여 저장해야 하며, 평문 노출 금지.
- emailId는 중복 불가, role 값은 시스템 정책에 따라 제한될 수 있음.
