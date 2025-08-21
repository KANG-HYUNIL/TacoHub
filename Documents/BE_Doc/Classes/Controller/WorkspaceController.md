# WorkspaceController

**패키지:** com.example.TacoHub.Controller.NotionCopyController

## 개요
워크스페이스 관리를 위한 REST API Controller입니다. 워크스페이스의 CRUD 기능과 사용자 관리 기능을 제공합니다.

## 클래스 구조

### 어노테이션
- `@RestController`: REST API 컨트롤러로 등록
- `@RequestMapping("/api/workspaces")`: 기본 URL 경로 설정
- `@RequiredArgsConstructor`: Lombok을 통한 의존성 주입 생성자 자동 생성
- `@Slf4j`: 로깅 기능 제공

### 멤버 변수
- `WorkSpaceService workspaceService`: 워크스페이스 비즈니스 로직 처리

## API 엔드포인트

### 기본 경로
- **Base URL**: `/api/workspaces`
- **Content-Type**: `application/json`

## 메서드 상세

### 1. 워크스페이스 생성
```http
POST /api/workspaces
```

**요청:**
```json
{
  "name": "새 워크스페이스"
}
```

**응답:**
```json
{
  "success": true,
  "message": "워크스페이스가 성공적으로 생성되었습니다.",
  "data": {
    "id": "uuid",
    "name": "새 워크스페이스",
    "createdAt": "2025-01-09T10:00:00",
    "updatedAt": "2025-01-09T10:00:00"
  }
}
```

**상태 코드:** 201 Created

### 2. 워크스페이스 조회
```http
GET /api/workspaces/{workspaceId}
```

**응답:**
```json
{
  "success": true,
  "message": "워크스페이스 조회가 완료되었습니다.",
  "data": {
    "id": "uuid",
    "name": "워크스페이스명",
    "createdAt": "2025-01-09T10:00:00",
    "updatedAt": "2025-01-09T10:00:00"
  }
}
```

**상태 코드:** 200 OK

### 3. 워크스페이스 삭제
```http
DELETE /api/workspaces/{workspaceId}
```

**응답:**
```json
{
  "success": true,
  "message": "워크스페이스가 성공적으로 삭제되었습니다.",
  "data": null
}
```

**상태 코드:** 200 OK

### 4. 사용자 초대 (TODO)
```http
POST /api/workspaces/{workspaceId}/invite
```

**요청:**
```json
{
  "email": "user@example.com",
  "role": "MEMBER",
  "message": "워크스페이스에 초대합니다.",
  "expirationDays": 7
}
```

### 5. 사용자 역할 변경 (TODO)
```http
PUT /api/workspaces/{workspaceId}/users/role
```

### 6. 사용자 제거 (TODO)
```http
DELETE /api/workspaces/{workspaceId}/users
```

### 7. 워크스페이스 멤버 조회 (TODO)
```http
GET /api/workspaces/{workspaceId}/members
```

## 구현 상태

### 완료된 기능
- ✅ 워크스페이스 생성 (createWorkspace)
- ✅ 워크스페이스 조회 (getWorkspace)
- ✅ 워크스페이스 삭제 (deleteWorkspace)

### 미완료된 기능 (TODO)
- ⏳ 사용자 초대 (inviteUser)
- ⏳ 사용자 역할 변경 (updateUserRole)
- ⏳ 사용자 제거 (removeUser)
- ⏳ 멤버 목록 조회 (getWorkspaceMembers)
- ⏳ 초대 수락 (acceptInvitation)
- ⏳ 초대 거절 (declineInvitation)
- ⏳ 초대 재발송 (resendInvitation)
- ⏳ 초대 취소 (cancelInvitation)
- ⏳ 대기 중인 초대 목록 조회 (getPendingInvitations)
- ⏳ 사용자 워크스페이스 목록 조회 (getMyWorkspaces)
- ⏳ 워크스페이스 이름 수정 (updateWorkspaceName)
- ⏳ 워크스페이스 나가기 (leaveWorkspace)



```

## 의존성
- **WorkSpaceService**: 워크스페이스 비즈니스 로직
- **WorkSpaceConverter**: Entity ↔ DTO 변환

## 로깅
- 모든 요청/응답에 대한 INFO 레벨 로깅
- 에러 발생 시 ERROR 레벨 로깅
- 주요 작업 완료 시점 로깅

## 향후 개선사항
1. 페이지네이션 지원
2. 워크스페이스 검색 기능
3. 워크스페이스 멤버 관리 통합
4. 워크스페이스 템플릿 기능
