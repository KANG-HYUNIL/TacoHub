# WorkSpaceService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
- 워크스페이스 생성 및 관리를 담당하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `WorkSpaceRepository workspaceRepository`: 워크스페이스 데이터 접근
- `PageService pageService`: 기본 페이지 생성 등 연동

## 주요 메서드
- `createWorkspaceEntity(String newWorkspaceName)`: 워크스페이스 생성

## 동작 흐름
1. 입력값 검증(이름 등)
2. 워크스페이스 Entity 생성/저장
3. 기본 페이지 자동 생성

## 예시
```java
workSpaceService.createWorkspaceEntity("새 워크스페이스");
```

## 활용
- Notion 스타일 워크스페이스 관리, 기본 페이지 자동화
