# EmailVerificationDto

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto</td></tr>
  <tr><th>클래스 설명</th><td>이메일 인증 요청/검증에 사용되는 DTO 클래스.<br>회원가입, 비밀번호 찾기 등 다양한 인증 목적에 활용된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>email</td><td>String</td><td>이메일 주소. 형식 검증 필요.</td><td>"user@example.com"</td></tr>
  <tr><td>authCode</td><td>String</td><td>인증 코드. 서버에서 생성 및 검증.</td><td>"123456"</td></tr>
  <tr><td>purpose</td><td>String</td><td>인증 목적(회원가입, 비밀번호 찾기 등).</td><td>"회원가입", "비밀번호 찾기"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>EmailVerificationDto()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>EmailVerificationDto(email, authCode, purpose)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>각 필드별(String email, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. 인증 요청 시 EmailVerificationDto 객체가 생성된다.
2. 각 필드에 값이 할당되어 인증 정보가 전달된다.
3. 인증코드 검증, 인증 목적별 분기 처리 등에 활용된다.

## 활용 예시 (Usage)
이메일 인증 요청:
```json
{
  "email": "user@example.com",
  "authCode": "123456",
  "purpose": "회원가입"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- email 필드는 반드시 올바른 이메일 형식이어야 함.
- authCode는 서버에서 생성 및 검증되며, 유효기간 내에만 사용 가능.
