# PageService

**패키지:** com.example.TacoHub.Service.NotionCopyService

## 개요
- Notion 스타일의 페이지 생성, 복사, 관리를 담당하는 서비스 클래스입니다.

## 주요 멤버 및 의존성
- `PageRepository pageRepository`: 페이지 데이터 접근
- `BlockService blockService`: 블록 관리
- `WorkSpaceRepository workspaceRepository`: 워크스페이스 데이터 접근

## 주요 메서드
- `copyPage(UUID pageId, UUID workspaceId, UUID parentPageId)`: 페이지 복사
- `createPageEntity(UUID workspaceId, UUID parentPageId)`: 페이지 생성

## 동작 흐름
1. 입력값 검증
2. Entity 생성/저장
3. 블록/워크스페이스 연동

## 예시
```java
pageService.createPageEntity(workspaceId, null);
```

## 활용
- 워크스페이스 내 페이지 생성/복사/관리
