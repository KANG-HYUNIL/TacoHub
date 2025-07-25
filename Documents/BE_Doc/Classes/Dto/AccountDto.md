# AccountDto

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.AccountDto</td></tr>
  <tr><th>클래스 설명</th><td>사용자 계정 정보를 계층 간 안전하게 전달하는 DTO(Data Transfer Object) 클래스.<br>계정 생성, 조회, 수정 등 다양한 API 요청/응답에 활용된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>emailId</td><td>String</td><td>사용자를 식별하는 고유 이메일 주소.</td><td>"user@example.com"</td></tr>
  <tr><td>password</td><td>String</td><td>사용자 비밀번호. 서버에서 암호화 처리됨.</td><td>"password123" (API 요청 시 평문, 저장 시 암호화)</td></tr>
  <tr><td>name</td><td>String</td><td>사용자의 실제 이름.</td><td>"홍길동"</td></tr>
  <tr><td>role</td><td>String</td><td>사용자의 시스템 내 권한 레벨.</td><td>"ROLE_USER", "ROLE_LEADER"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>AccountDto()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>AccountDto(emailId, password, name, role)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>각 필드별(String emailId, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>부모 클래스</th><th>설명</th></tr>
  <tr><td>BaseDateDTO</td><td>생성일시(createdAt), 수정일시(updatedAt) 정보 포함.</td></tr>
</table>

## 관련 클래스 (Relations)
<table>
  <tr><th>종류</th><th>클래스명</th><th>설명</th></tr>
  <tr><td>Entity</td><td>AccountEntity</td><td>실제 DB 계정 엔티티</td></tr>
  <tr><td>Repository</td><td>AccountRepository</td><td>계정 DB 접근 레이어</td></tr>
  <tr><td>Service</td><td>AccountService</td><td>비즈니스 로직 처리</td></tr>
  <tr><td>Controller</td><td>AccountController</td><td>API 엔드포인트</td></tr>
  <tr><td>Converter</td><td>AccountConverter</td><td>Entity-DTO 변환</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. API 요청/응답 시 AccountDto 객체가 생성된다.
2. 각 필드에 값이 할당되어 계층 간 데이터가 전달된다.
3. Entity 변환, DB 저장, 비즈니스 로직 처리 등에 활용된다.

## 활용 예시 (Usage)
회원가입 요청:
```json
{
  "emailId": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "role": "ROLE_USER"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- password는 반드시 암호화하여 저장해야 하며, 평문 노출 금지.
- role 값은 시스템 정책에 따라 제한될 수 있음.

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
