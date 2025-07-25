# ResendInvitationRequest

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO.Request</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 초대 재발송 요청을 위한 DTO(Data Transfer Object) 클래스.<br>초대 대상 이메일, 워크스페이스 ID 등 서버로 전달.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>workspaceId</td><td>UUID</td><td>초대 재발송이 이루어질 워크스페이스의 ID.</td></tr>
  <tr><td>invitedEmail</td><td>String</td><td>초대 대상 이메일 주소.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>ResendInvitationRequest()</td><td>기본 생성자.</td></tr>
  <tr><td>ResendInvitationRequest(workspaceId, invitedEmail)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 활용 예시 (Usage)
초대 재발송 요청:
```json
{
  "workspaceId": "w1a2c3d4-...",
  "invitedEmail": "invitee@example.com"
}
```
