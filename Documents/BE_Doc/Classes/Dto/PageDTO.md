# PageDTO

**패키지:** com.example.TacoHub.Dto.NotionCopyDTO

## 개요
페이지 정보 전송을 위한 DTO(Data Transfer Object) 클래스입니다. Notion 스타일의 계층형 페이지 구조와 워크스페이스 연동 정보를 클라이언트와 서버 간에 주고받기 위한 데이터 구조를 제공합니다.

## 클래스 구조

### 어노테이션
- `@Data`: Lombok을 통한 getter/setter/toString/equals/hashCode 자동 생성
- `@SuperBuilder`: 부모 클래스와 함께 빌더 패턴 지원
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성
- `@EqualsAndHashCode(callSuper = true)`: 부모 클래스 필드 포함한 equals/hashCode

### 상속 관계
- `extends BaseDateDTO`: 생성일시/수정일시 등 공통 필드 상속

## 필드 구조

### 1. 기본 페이지 정보
```java
private UUID id;
```
- **목적**: 페이지의 고유 식별자
- **타입**: UUID
- **null 허용**: 새 페이지 생성 시에는 null, 서버에서 자동 생성

```java
private String title;
```
- **목적**: 페이지 제목
- **사용자 편집**: 사용자가 직접 수정 가능
- **기본값**: "New Page" (서비스에서 설정)

```java
private String path;
```
- **목적**: 페이지 경로 (URL 또는 계층 경로)
- **활용**: 
  - 브레드크럼 네비게이션
  - URL 라우팅
  - 페이지 검색
- **예시**: `/workspace/project/design/mockup`

```java
private Integer orderIndex;
```
- **목적**: 같은 부모 하위에서의 페이지 순서
- **시작값**: 0
- **활용**: 사이드바, 네비게이션에서의 페이지 순서

```java
private Boolean isRoot;
```
- **목적**: 루트 페이지 여부 식별
- **true**: 워크스페이스 직속 루트 페이지
- **false**: 다른 페이지의 하위 페이지

### 2. 워크스페이스 연관 정보
```java
private UUID workspaceId;
```
- **목적**: 페이지가 속한 워크스페이스 ID
- **필수성**: 모든 페이지는 특정 워크스페이스에 소속
- **활용**: 워크스페이스별 페이지 필터링

```java
private String workspaceName;
```
- **목적**: 워크스페이스 이름 (비정규화)
- **편의성**: 페이지 정보 조회 시 별도 워크스페이스 조회 불필요
- **주의**: 워크스페이스 이름 변경 시 동기화 필요

### 3. 계층 구조 정보
```java
private UUID parentPageId;
```
- **목적**: 부모 페이지 ID
- **null 의미**: 루트 페이지 (워크스페이스 직속)
- **제약**: 부모와 자식은 같은 워크스페이스에 속해야 함

```java
private List<PageDTO> childPages;
```
- **목적**: 자식 페이지들의 전체 정보
- **타입**: PageDTO 리스트 (재귀적 구조)
- **정렬**: orderIndex 기준 자동 정렬
- **깊이**: 계층 깊이에 따른 성능 고려 필요

### 4. 상속된 필드 (BaseDateDTO)
```java
// BaseDateDTO에서 상속
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private String createdBy;
private String lastEditedBy;
```

## 계층 구조 표현

### JSON 구조 예시
```json
{
  "id": "root-page-id",
  "title": "프로젝트 메인",
  "path": "/workspace/project",
  "workspaceId": "workspace-id",
  "workspaceName": "내 워크스페이스",
  "isRoot": true,
  "parentPageId": null,
  "orderIndex": 0,
  "childPages": [
    {
      "id": "child-page-1",
      "title": "디자인",
      "path": "/workspace/project/design",
      "workspaceId": "workspace-id",
      "workspaceName": "내 워크스페이스",
      "isRoot": false,
      "parentPageId": "root-page-id",
      "orderIndex": 0,
      "childPages": [
        {
          "id": "grandchild-page-1",
          "title": "목업",
          "path": "/workspace/project/design/mockup",
          "isRoot": false,
          "parentPageId": "child-page-1",
          "orderIndex": 0,
          "childPages": []
        }
      ]
    },
    {
      "id": "child-page-2",
      "title": "개발",
      "isRoot": false,
      "parentPageId": "root-page-id",
      "orderIndex": 1,
      "childPages": []
    }
  ]
}
```

## 사용 패턴

### 1. 루트 페이지 조회
```java
// 워크스페이스의 모든 루트 페이지
List<PageDTO> rootPages = pageService.getRootPages(workspaceId);

// 특정 루트 페이지와 전체 계층 구조
PageDTO pageWithChildren = pageService.getPageWithChildren(pageId);
```

### 2. 계층 네비게이션
```java
// 브레드크럼 생성
public List<PageDTO> getBreadcrumb(UUID pageId) {
    List<PageDTO> breadcrumb = new ArrayList<>();
    PageDTO current = pageService.getPage(pageId);
    
    while (current != null) {
        breadcrumb.add(0, current);
        if (current.getParentPageId() != null) {
            current = pageService.getPage(current.getParentPageId());
        } else {
            break;
        }
    }
    
    return breadcrumb;
}
```

### 3. 사이드바 렌더링
```typescript
// TypeScript 프론트엔드 예시
interface PageTreeNode {
  page: PageDTO;
  children: PageTreeNode[];
  isExpanded: boolean;
}

const renderSidebar = (pages: PageDTO[]): PageTreeNode[] => {
  return pages.map(page => ({
    page,
    children: renderSidebar(page.childPages || []),
    isExpanded: false
  }));
};
```

## 성능 고려사항

### 1. 지연 로딩 vs 즉시 로딩
```java
// 얕은 조회 (자식 페이지 제외)
PageDTO getPageShallow(UUID pageId);

// 깊은 조회 (전체 계층 포함)
PageDTO getPageWithChildren(UUID pageId);

// 특정 깊이까지만 조회
PageDTO getPageWithChildren(UUID pageId, int maxDepth);
```

### 2. 순환 참조 방지
```java
@JsonIgnore
private PageDTO parentPage; // 부모 페이지 전체 정보는 제외

// 또는 별도 메서드로 제공
public PageDTO getParentPage() {
    if (parentPageId != null) {
        return pageService.getPage(parentPageId);
    }
    return null;
}
```

### 3. 대용량 계층 처리
```java
// 페이징된 자식 페이지 조회
public Page<PageDTO> getChildPages(UUID parentPageId, Pageable pageable) {
    return pageService.getChildPages(parentPageId, pageable);
}
```

## 비즈니스 로직 연동

### 1. 페이지 생성
```java
public PageDTO createPage(CreatePageRequest request) {
    PageDTO newPage = PageDTO.builder()
        .title(request.getTitle())
        .workspaceId(request.getWorkspaceId())
        .parentPageId(request.getParentPageId())
        .isRoot(request.getParentPageId() == null)
        .orderIndex(getNextOrderIndex(request.getParentPageId()))
        .build();
    
    return pageService.createPage(newPage);
}
```

### 2. 페이지 이동
```java
public PageDTO movePage(UUID pageId, UUID newParentId, Integer newOrderIndex) {
    PageDTO page = pageService.getPage(pageId);
    page.setParentPageId(newParentId);
    page.setIsRoot(newParentId == null);
    page.setOrderIndex(newOrderIndex);
    
    return pageService.updatePage(page);
}
```

### 3. 페이지 삭제
```java
public void deletePage(UUID pageId) {
    PageDTO page = pageService.getPage(pageId);
    
    // 자식 페이지 처리 확인
    if (page.getChildPages() != null && !page.getChildPages().isEmpty()) {
        throw new PageOperationException("자식 페이지가 있는 페이지는 삭제할 수 없습니다");
    }
    
    pageService.deletePage(pageId);
}
```

## 프론트엔드 연동

### TypeScript 인터페이스
```typescript
interface PageDTO {
  id?: string;
  title: string;
  path?: string;
  orderIndex: number;
  isRoot: boolean;
  
  workspaceId: string;
  workspaceName?: string;
  
  parentPageId?: string;
  childPages?: PageDTO[];
  
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  lastEditedBy?: string;
}
```

### REST API 예시
```typescript
// 페이지 조회 (자식 포함)
const getPageWithChildren = async (pageId: string): Promise<PageDTO> => {
  const response = await fetch(`/api/pages/${pageId}?includeChildren=true`);
  return response.json();
};

// 워크스페이스 페이지 트리
const getWorkspacePages = async (workspaceId: string): Promise<PageDTO[]> => {
  const response = await fetch(`/api/workspaces/${workspaceId}/pages`);
  return response.json();
};

// 페이지 생성
const createPage = async (pageData: Partial<PageDTO>): Promise<PageDTO> => {
  const response = await fetch('/api/pages', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(pageData)
  });
  return response.json();
};
```

## 설계 이슈 및 개선 방향

### 현재 설계의 특징
1. **명확한 역할 분리**: 페이지 메타데이터와 블록 컨텐츠 분리
2. **워크스페이스 정보 포함**: workspaceName으로 편의성 제공
3. **계층 구조 지원**: 부모-자식 관계 및 순서 관리
4. **단방향 참조**: 블록과의 복잡한 양방향 참조 제거

### 권장 개선 방향
1. **성능 최적화**: 대량 childPages 로딩 시 지연 로딩 고려
2. **워크스페이스 정보 분리**: 필요시 별도 조회 또는 Join 활용
3. **계층 깊이 제한**: 최대 5-7단계 제한
4. **블록 연동**: 별도 BlockService를 통한 컨텐츠 관리

### 개선된 구조 특징
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO extends BaseDateDTO {
    private UUID id;
    private String title;
    private String path;
    private Integer orderIndex;
    private Boolean isRoot;
    
    private UUID workspaceId;
    private String workspaceName; // 편의성을 위한 비정규화
    private UUID parentPageId;
    private List<PageDTO> childPages; // 계층 구조 지원
    
    // 블록 컨텐츠는 별도 BlockService에서 관리
    // pageId를 통해 MongoDB의 블록들을 조회
}
```

## 사용 예시

### 기본 페이지 생성
```java
PageDTO rootPage = PageDTO.builder()
    .title("새 페이지")
    .workspaceId(workspaceId)
    .isRoot(true)
    .orderIndex(0)
    .build();
```

### 자식 페이지 생성
```java
PageDTO childPage = PageDTO.builder()
    .title("하위 페이지")
    .workspaceId(workspaceId)
    .parentPageId(parentPageId)
    .isRoot(false)
    .orderIndex(1)
    .build();
```

### 페이지 트리 탐색
```java
public void printPageTree(PageDTO page, int depth) {
    String indent = "  ".repeat(depth);
    System.out.println(indent + page.getTitle());
    
    if (page.getChildPages() != null) {
        for (PageDTO child : page.getChildPages()) {
            printPageTree(child, depth + 1);
        }
    }
}
```

## 연관 클래스
- **PageEntity**: JPA 엔티티
- **PageService**: 비즈니스 로직
- **PageController**: REST API
- **PageConverter**: Entity ↔ DTO 변환
- **WorkSpaceDTO**: 워크스페이스 정보
- **BlockDTO**: 연결된 블록 정보

## 주의사항
1. **계층 일관성**: parentPageId와 childPages의 양방향 일관성 유지
2. **워크스페이스 제약**: 부모-자식 페이지는 같은 워크스페이스에 속해야 함
3. **순환 참조 방지**: 부모-자식 관계에서 순환 구조 생성 금지
4. **성능 모니터링**: 깊은 계층 구조 조회 시 성능 영향
5. **JSON 크기**: childPages 포함 시 응답 크기 주의
6. **동기화**: 워크스페이스 이름 변경 시 관련 PageDTO 업데이트
7. **null 처리**: 선택적 필드의 null 안전 처리
