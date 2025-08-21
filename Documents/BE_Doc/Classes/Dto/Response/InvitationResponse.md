# InvitationResponse

**패키지:** com.example.TacoHub.Dto.NotionCopyDTO.Response

## 개요
워크스페이스 초대 발송 완료 응답을 위한 DTO(Data Transfer Object) 클래스입니다. 사용자 초대 요청 처리 후 클라이언트에게 초대 상태, 토큰 정보, 발송 결과 등을 전달하기 위한 데이터 구조를 제공합니다.

## 클래스 구조

### 어노테이션
- `@Data`: Lombok을 통한 getter/setter/toString/equals/hashCode 자동 생성
- `@Builder`: Lombok 빌더 패턴 지원
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성

### 특징
- **초대 추적**: 발송된 초대의 완전한 상태 정보 제공
- **보안 정보**: 토큰은 포함하되 민감한 정보는 제외
- **시간 정보**: 만료 시간 등 시간 관련 정보 포함

## 필드 구조

### 1. 초대 토큰
```java
private String invitationToken;
```
- **목적**: 초대 수락에 사용될 고유 토큰
- **보안**: URL-safe 문자열, 추측 불가능한 랜덤 값
- **활용**: 초대 링크 생성, 수락 시 검증
- **주의**: 클라이언트에게 전달되므로 민감한 정보 포함 금지

### 2. 초대된 이메일
```java
private String invitedEmail;
```
- **목적**: 초대 대상자의 이메일 주소
- **활용**: 초대 확인, 관리자 화면 표시
- **검증**: 유효한 이메일 형식 보장

### 3. 부여될 역할
```java
# InvitationResponse

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO.Response</td></tr>
  <tr><th>클래스 설명</th><td>워크스페이스 초대 발송 완료 응답을 위한 DTO(Data Transfer Object) 클래스.<br>사용자 초대 요청 처리 후 클라이언트에 초대 상태, 토큰 정보, 발송 결과 등을 전달한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>invitationToken</td><td>String</td><td>초대 수락에 사용될 고유 토큰. URL-safe, 랜덤 값.</td><td>"eyJhbGciOiJIUzI1NiIsInR5cCI6..."</td></tr>
  <tr><td>invitedEmail</td><td>String</td><td>초대 대상자의 이메일 주소. 유효한 이메일 형식.</td><td>"invitee@example.com"</td></tr>
  <tr><td>role</td><td>String</td><td>초대 수락 시 부여될 워크스페이스 역할.</td><td>"ADMIN", "MEMBER", "GUEST"</td></tr>
  <tr><td>invitedAt</td><td>LocalDateTime</td><td>초대가 발송된 시각. ISO-8601 포맷.</td><td>"2024-01-15T10:30:00"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>InvitationResponse()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>InvitationResponse(invitationToken, invitedEmail, role, invitedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>각 필드별(String invitationToken, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 동작 흐름 (Lifecycle)
1. 초대 발송 완료 시 InvitationResponse 객체가 생성된다.
2. 각 필드에 값이 할당되어 클라이언트에 전달된다.
3. 초대 링크 생성, 수락 시 검증 등에 활용된다.

## 활용 예시 (Usage)
초대 발송 응답:
```json
{
  "invitationToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "invitedEmail": "invitee@example.com",
  "role": "MEMBER",
  "invitedAt": "2024-01-15T10:30:00"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- invitationToken은 외부에 노출 시 보안에 주의.
- invitedEmail은 반드시 유효한 이메일 형식이어야 함.
  "invitedEmail": "colleague@company.com",
  "role": "MEMBER",
  "invitedAt": "2025-07-09T14:30:00",
  "expiresAt": "2025-07-16T14:30:00",
  "invitedBy": "admin@company.com",
  "message": "프로젝트 Alpha에 참여해주세요!",
  "isEmailSent": true
}
```

### 관리자 초대 (긴급)
```json
{
  "invitationToken": "URGENT-ADMIN-TOKEN-ABC123",
  "invitedEmail": "manager@company.com",
  "role": "ADMIN",
  "invitedAt": "2025-07-09T09:00:00",
  "expiresAt": "2025-07-10T09:00:00",
  "invitedBy": "ceo@company.com",
  "message": "긴급 프로젝트 관리를 부탁드립니다.",
  "isEmailSent": true
}
```

### 이메일 발송 실패한 초대
```json
{
  "invitationToken": "TOKEN-WITH-EMAIL-FAILURE-456",
  "invitedEmail": "invalid@bounced-domain.com",
  "role": "GUEST",
  "invitedAt": "2025-07-09T16:45:00",
  "expiresAt": "2025-07-16T16:45:00",
  "invitedBy": "admin@company.com",
  "message": null,
  "isEmailSent": false
}
```

### 외부 협력사 초대
```json
{
  "invitationToken": "EXTERNAL-PARTNER-TOKEN-789",
  "invitedEmail": "partner@external.com",
  "role": "GUEST",
  "invitedAt": "2025-07-09T13:15:00",
  "expiresAt": "2025-08-08T13:15:00",
  "invitedBy": "projectlead@company.com",
  "message": "프로젝트 진행상황을 공유드립니다. 30일간 유효합니다.",
  "isEmailSent": true
}
```

## 활용 시나리오

### 1. 초대 발송 완료 응답
```java
@PostMapping("/{workspaceId}/invite")
public ResponseEntity<ApiResponse<InvitationResponse>> inviteUser(
    @PathVariable UUID workspaceId,
    @RequestBody InviteUserRequest request) {
    
    InvitationResponse invitation = workspaceUserService.inviteUser(workspaceId, request);
    
    if (!invitation.isEmailSent()) {
        return ResponseEntity.ok(ApiResponse.success(
            "초대가 생성되었지만 이메일 발송에 실패했습니다. 재전송을 시도해주세요.", 
            invitation));
    }
    
    return ResponseEntity.ok(ApiResponse.success(
        "초대 이메일이 성공적으로 발송되었습니다.", 
        invitation));
}
```

### 2. 초대 상태 확인
```java
public void checkInvitationStatus(InvitationResponse invitation) {
    if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
        log.warn("만료된 초대: {}", invitation.getInvitedEmail());
        // 재발송 안내 또는 자동 연장 로직
    }
    
    if (!invitation.isEmailSent()) {
        log.error("이메일 발송 실패: {}", invitation.getInvitedEmail());
        // 재전송 로직 또는 알림
    }
}
```

### 3. 관리자 대시보드 표시
```java
public List<InvitationResponse> getPendingInvitations(UUID workspaceId) {
    return invitationService.getPendingInvitations(workspaceId)
        .stream()
        .filter(inv -> inv.getExpiresAt().isAfter(LocalDateTime.now()))
        .sorted(Comparator.comparing(InvitationResponse::getInvitedAt).reversed())
        .collect(Collectors.toList());
}
```

## 클라이언트 활용

### 1. 초대 결과 표시
```typescript
interface InvitationResponse {
  invitationToken: string;
  invitedEmail: string;
  role: 'ADMIN' | 'MEMBER' | 'GUEST';
  invitedAt: string;
  expiresAt: string;
  invitedBy: string;
  message?: string;
  isEmailSent: boolean;
}

// 초대 결과 처리
const handleInvitationResult = (response: InvitationResponse) => {
  if (response.isEmailSent) {
    showSuccess(`${response.invitedEmail}에게 초대 이메일을 발송했습니다.`);
  } else {
    showWarning(`초대는 생성되었지만 이메일 발송에 실패했습니다.`);
  }
};
```

### 2. 초대 링크 생성
```typescript
const generateInvitationLink = (response: InvitationResponse) => {
  return `${window.location.origin}/accept-invitation?token=${response.invitationToken}`;
};
```

### 3. 만료 시간 표시
```typescript
const formatExpirationTime = (expiresAt: string) => {
  const expiration = new Date(expiresAt);
  const now = new Date();
  const diffMs = expiration.getTime() - now.getTime();
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  
  if (diffDays > 0) {
    return `${diffDays}일 후 만료`;
  } else {
    return '만료됨';
  }
};
```

## 보안 고려사항

### 1. 토큰 보안
- **길이**: 최소 32자 이상의 랜덤 문자열
- **예측 불가**: 암호학적으로 안전한 랜덤 생성
- **일회성**: 사용 후 즉시 무효화

### 2. 정보 노출 제한
- **민감 정보**: 토큰에 사용자 ID 등 민감 정보 포함 금지
- **만료 처리**: 만료된 토큰 정보는 삭제 또는 마스킹
- **로깅**: 토큰 값은 로그에 기록하지 않음

### 3. 이메일 보안
- **스팸 방지**: 발송 빈도 제한
- **인증**: SPF, DKIM 등 이메일 인증 설정
- **추적**: 이메일 열람 및 클릭 추적

## 확장 가능성

### 1. 추가 메타데이터
```java
private String workspaceName;       // 워크스페이스 이름 (편의성)
private int resendCount;           // 재전송 횟수
private String ipAddress;          // 초대 발송 IP
private String userAgent;          // 초대 발송 브라우저
```

### 2. 알림 설정
```java
private boolean sendSmsNotification;    // SMS 알림 여부
private boolean sendSlackNotification;  // Slack 알림 여부
private String notificationChannel;     // 알림 채널
```

## 관련 클래스
- **요청**: `InviteUserRequest`, `ResendInvitationRequest`
- **엔티티**: `WorkSpaceUserEntity` (수락 후 생성)
- **서비스**: `WorkSpaceUserService`, `EmailService`, `AuthCodeService`
- **컨트롤러**: `WorkspaceController`
