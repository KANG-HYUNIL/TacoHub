# LogInDto

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto</td></tr>
  <tr><th>클래스 설명</th><td>로그인 요청에 사용되는 DTO 클래스.<br>사용자 인증, 세션 생성 등 다양한 로그인 관련 기능에 활용된다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>emailId</td><td>String</td><td>로그인에 사용되는 이메일 ID.</td><td>"user@example.com"</td></tr>
  <tr><td>password</td><td>String</td><td>로그인에 사용되는 비밀번호. 서버에서 암호화 처리됨.</td><td>"pw1234"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>LogInDto()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>LogInDto(emailId, password)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
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

## 동작 흐름 (Lifecycle)
1. 로그인 요청 시 LogInDto 객체가 생성된다.
2. 각 필드에 값이 할당되어 인증 정보가 전달된다.
3. 인증 처리, 세션 생성 등에 활용된다.

## 활용 예시 (Usage)
로그인 요청:
```json
{
  "emailId": "user@example.com",
  "password": "pw1234"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- password는 반드시 암호화하여 저장해야 하며, 평문 노출 금지.
- emailId는 올바른 이메일 형식이어야 함.
