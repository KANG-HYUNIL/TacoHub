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
private String role;
```
- **목적**: 초대 수락 시 부여될 워크스페이스 역할
- **값**: ADMIN, MEMBER, GUEST
- **활용**: 초대 내용 확인, 권한 미리보기

### 4. 초대 시간
```java
private LocalDateTime invitedAt;
```
- **목적**: 초대가 발송된 시점
- **활용**: 초대 이력 관리, 정렬
- **형식**: ISO 8601 형태의 로컬 날짜/시간

### 5. 만료 시간
```java
private LocalDateTime expiresAt;
```
- **목적**: 초대 토큰의 만료 시점
- **활용**: 유효성 확인, 남은 시간 표시
- **계산**: 초대 시간 + 설정된 만료 기간

### 6. 초대한 사용자
```java
private String invitedBy;
```
- **목적**: 초대를 발송한 사용자 식별
- **활용**: 감사 추적, 초대 이력 관리
- **형식**: 일반적으로 이메일 주소

### 7. 초대 메시지
```java
private String message;
```
- **목적**: 초대 시 포함된 개인 메시지
- **필수성**: 선택적 필드 (null 가능)
- **활용**: 초대 이메일 내용, 맥락 제공

### 8. 이메일 발송 상태
```java
private boolean isEmailSent;
```
- **목적**: 초대 이메일 발송 성공 여부
- **활용**: 발송 실패 시 재전송 필요성 판단
- **오류 처리**: false인 경우 추가 조치 필요

## JSON 예시

### 성공적인 초대 발송
```json
{
  "invitationToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ",
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
