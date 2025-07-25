# InviteUserRequest

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO.Request</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 사용자 초대 요청을 위한 DTO(Data Transfer Object) 클래스.<br>초대 대상 이메일, 역할 등 서버로 전달.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th></tr>
  <tr><td>workspaceId</td><td>UUID</td><td>초대가 이루어질 워크스페이스의 ID.</td></tr>
  <tr><td>invitedEmail</td><td>String</td><td>초대 대상 이메일 주소.</td></tr>
  <tr><td>role</td><td>String</td><td>초대 시 부여할 역할.</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>InviteUserRequest()</td><td>기본 생성자.</td></tr>
  <tr><td>InviteUserRequest(workspaceId, invitedEmail, role)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 활용 예시 (Usage)
사용자 초대 요청:
```json
{
  "workspaceId": "w1a2c3d4-...",
  "invitedEmail": "invitee@example.com",
  "role": "MEMBER"
}
```
