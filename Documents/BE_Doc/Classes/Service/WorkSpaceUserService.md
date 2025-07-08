# WorkSpaceUserService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
- 사용자-워크스페이스 관계 관리 및 권한 확인을 담당하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `WorkSpaceUserRepository workSpaceUserRepository`: 관계 데이터 접근

## 주요 메서드
- `getWorkSpaceUserEntity(String userEmailId, UUID workspaceId)`: 관계 조회
- `canUserManageWorkSpace(String userEmailId, UUID workspaceId)`: 관리 권한 확인

## 동작 흐름
1. 입력값 검증
2. 관계 조회 및 예외 처리
3. 권한 확인

## 예시
```java
boolean canManage = workSpaceUserService.canUserManageWorkSpace(userEmailId, workspaceId);
```

## 활용
- 워크스페이스 멤버십, 권한 관리, 협업 기능
