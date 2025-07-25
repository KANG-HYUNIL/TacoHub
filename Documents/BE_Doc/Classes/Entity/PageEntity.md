# PageEntity

**패키지:** com.example.TacoHub.Entity.NotionCopyEntity

## 개요
Notion 스타일의 페이지를 나타내는 JPA 엔티티 클래스입니다. 워크스페이스 내에서 계층형 페이지 구조를 지원하며, 각 페이지는 연결된 블록 시스템을 통해 컨텐츠를 관리합니다.

## 클래스 구조

### 어노테이션
- `@Entity`: JPA 엔티티로 지정
- `@Getter`, `@Setter`: Lombok을 통한 getter/setter 자동 생성
- `@Builder`: Lombok 빌더 패턴 지원
- `@Table(name = "page")`: 데이터베이스 테이블명 명시적 지정

### 상속 관계
- `extends BaseDateEntity`: 생성일시/수정일시 등 공통 필드 상속

## 필드 구조

### 1. 기본 식별자
```java
@Id
@GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
@Column(name = "id", updatable = false, nullable = false)
# PageEntity

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Entity.NotionCopyEntity</td></tr>
  <tr><th>클래스 설명</th><td>Notion 스타일의 페이지를 나타내는 JPA 엔티티(Entity) 클래스.<br>워크스페이스 내 계층형 페이지 구조를 지원하며, 각 페이지는 연결된 블록 시스템을 통해 콘텐츠를 관리한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>제약/예시</th></tr>
  <tr><td>id</td><td>UUID</td><td>페이지의 고유 식별자. 글로벌 고유성 보장.</td><td>PRIMARY KEY, NOT NULL</td></tr>
  <tr><td>title</td><td>String</td><td>페이지 제목. 사용자가 편집 가능.</td><td>"프로젝트 계획서"</td></tr>
  <tr><td>path</td><td>String</td><td>페이지 경로(URL 또는 계층 경로).</td><td>"/workspace/project/design/mockup"</td></tr>
  <tr><td>orderIndex</td><td>Integer</td><td>같은 부모 하위에서의 페이지 순서.</td><td>0, 1, 2...</td></tr>
  <tr><td>workspaceId</td><td>UUID</td><td>연동된 워크스페이스의 ID.</td><td>"w1a2c3d4-..."</td></tr>
  <tr><td>parentId</td><td>UUID</td><td>상위 페이지의 ID(계층 구조).</td><td>"p1a2c3d4-..."</td></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>페이지 생성 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>페이지 수정 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>PageEntity()</td><td>기본 생성자. JPA 및 Lombok에서 자동 생성.</td></tr>
  <tr><td>PageEntity(id, title, path, orderIndex, workspaceId, parentId, createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Getter/@Setter로 자동 생성.</td>
    <td>각 필드별(UUID id, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>부모 클래스</th><th>설명</th></tr>
  <tr><td>BaseDateEntity</td><td>생성일시(createdAt), 수정일시(updatedAt) 자동 관리.</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. 페이지 생성/조회/수정 등에서 PageEntity 객체가 생성된다.
2. 각 필드에 값이 할당되어 DB에 저장된다.
3. Entity-DTO 변환, 비즈니스 로직 처리 등에 활용된다.

## DB 테이블 예시 (Schema)
```sql
CREATE TABLE page (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    path VARCHAR(255),
    order_index INT,
    workspace_id UUID,
    parent_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 예외 및 주의사항 (Exceptions & Notes)
- id, title, path 등 필수값 누락 시 DB 저장이 거부될 수 있음.
- orderIndex는 같은 부모 하위에서 고유해야 함.
- **관계**: 자기 참조 다대일 관계
- **목적**: 부모 페이지 참조 (계층형 구조 지원)
- **null 허용**: 루트 페이지의 경우 null

```java
@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentPage", cascade = CascadeType.ALL)
@OrderBy("orderIndex ASC")
private List<PageEntity> childPages;
```
- **관계**: 일대다 (One-to-Many)
- **양방향 매핑**: parentPage 필드와 연결
- **CASCADE**: 부모 삭제 시 자식도 함께 삭제
- **정렬**: orderIndex 기준 오름차순 자동 정렬

### 6. 페이지 속성
```java
@Column(name = "order_index")
private Integer orderIndex;
```
- **목적**: 같은 부모 하위에서의 페이지 순서
- **타입**: Integer
- **활용**: 사이드바, 네비게이션에서의 페이지 순서 결정

```java
@Column(name="is_root", nullable = false)
private Boolean isRoot;
```
- **목적**: 루트 페이지 여부 식별
- **타입**: Boolean
- **제약**: null 불허
- **비즈니스 로직**: parentPage가 null인 경우 true

## 데이터베이스 스키마

### 테이블 구조
```sql
CREATE TABLE page (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    path VARCHAR(255),
    block_id UUID,
    workspace_id UUID NOT NULL,
    parent_page_id UUID,
    order_index INTEGER,
    is_root BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspace(id),
    FOREIGN KEY (parent_page_id) REFERENCES page(id)
);
```

### 인덱스 권장사항
- `workspace_id`: 워크스페이스별 페이지 조회 최적화
- `parent_page_id`: 자식 페이지 조회 최적화
- `order_index`: 정렬 성능 향상
- `is_root`: 루트 페이지 필터링 최적화

## 비즈니스 로직

### 페이지 계층 구조
- **루트 페이지**: `parentPage == null && isRoot == true`
- **자식 페이지**: `parentPage != null && isRoot == false`
- **순서 관리**: 같은 부모 하위에서 orderIndex로 순서 결정

### 연관관계 관리
- **워크스페이스 소속**: 모든 페이지는 반드시 워크스페이스에 소속
- **부모-자식 일관성**: 부모와 자식은 같은 워크스페이스에 속해야 함
- **CASCADE 삭제**: 부모 페이지 삭제 시 모든 자식 페이지도 삭제

### 블록 시스템 연동
- **설계 원칙**: 페이지는 MongoDB의 블록들에 대한 단방향 참조
- **Block 저장**: 페이지의 모든 블록은 MongoDB에 저장되며 pageId로 연결
- **관계 구조**: Page(PostgreSQL) → Block(MongoDB) 단방향 참조
- **컨텐츠 관리**: 실제 에디터 컨텐츠는 Block 시스템에서 완전히 관리

## 성능 고려사항

### 지연 로딩 전략
- 모든 연관관계가 LAZY 로딩으로 설정
- 필요시에만 연관 엔티티 로드하여 N+1 문제 방지

### 자식 페이지 정렬
- `@OrderBy("orderIndex ASC")`: 데이터베이스 레벨에서 자동 정렬
- 애플리케이션 레벨에서의 추가 정렬 불필요

### 계층 조회 최적화
- 깊은 계층 구조 시 재귀적 조회 성능 이슈 가능
- 향후 계층 깊이 제한 또는 Materialized Path 패턴 고려

## 설계 이슈 및 개선 방향

### 현재 설계의 특징
1. **명확한 역할 분리**: Page는 메타데이터, Block은 컨텐츠 담당
2. **단방향 참조**: Page ↔ Block 양방향 참조 없이 Block만 pageId 보유
3. **DB 특화**: PostgreSQL(관계형)과 MongoDB(문서형)의 장점 활용

### 권장 개선 방향
1. **컨텐츠 중심 설계**: Page는 컨테이너 역할, Block이 실제 컨텐츠 관리
2. **성능 최적화**: 대량 블록 데이터는 MongoDB에서 효율적으로 처리
3. **확장성 고려**: 블록 타입 추가 및 복잡한 에디터 기능 지원 용이
   
3. **옵션 3**: 역할 분리 명확화
   - Page: 네비게이션, 권한, 워크스페이스 관리
   - Block: 컨텐츠 구조 및 내용 관리

## 사용 예시

### 기본 페이지 생성
```java
PageEntity rootPage = PageEntity.builder()
    .title("새 페이지")
    .workspace(workspace)
    .parentPage(null)
    .isRoot(true)
    .orderIndex(0)
    .build();
```

### 자식 페이지 생성
```java
PageEntity childPage = PageEntity.builder()
    .title("자식 페이지")
    .workspace(workspace)
    .parentPage(rootPage)
    .isRoot(false)
    .orderIndex(1)
    .build();
```

### 계층 구조 탐색
```java
// 자식 페이지들 조회 (자동 정렬됨)
List<PageEntity> children = parentPage.getChildPages();

// 루트까지 거슬러 올라가기
PageEntity current = page;
while (current.getParentPage() != null) {
    current = current.getParentPage();
}
```

## 연관 클래스
- **WorkSpaceEntity**: 워크스페이스 정보
- **BaseDateEntity**: 공통 날짜 필드
- **BlockDocument**: 연결된 블록 정보 (MongoDB)
- **PageService**: 페이지 비즈니스 로직
- **PageRepository**: 데이터 접근 계층

## 주의사항
1. **순환 참조 방지**: 부모-자식 관계에서 순환 구조 생성 금지
2. **워크스페이스 일관성**: 부모와 자식 페이지는 같은 워크스페이스에 속해야 함
3. **삭제 주의**: CASCADE 설정으로 인한 대량 삭제 가능성
4. **Block 연동**: 페이지 삭제 시 연관된 Block들의 정리 필요
5. **성능 모니터링**: 깊은 계층 구조에서의 조회 성능 주의
