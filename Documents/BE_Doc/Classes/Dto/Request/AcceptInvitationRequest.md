# AcceptInvitationRequest

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO.Request</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 초대 수락 요청을 위한 DTO(Data Transfer Object) 클래스.<br>초대 토큰, 수락자 정보 등 서버로 전달.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>invitationToken</td><td>String</td><td>초대 수락에 사용되는 고유 토큰.</td></tr>
  <tr><td>acceptorEmail</td><td>String</td><td>초대 수락자 이메일 주소.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>AcceptInvitationRequest()</td><td>기본 생성자.</td></tr>
  <tr><td>AcceptInvitationRequest(invitationToken, acceptorEmail)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 활용 예시 (Usage)
초대 수락 요청:
```json
{
  "invitationToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "acceptorEmail": "user@example.com"
}
```
