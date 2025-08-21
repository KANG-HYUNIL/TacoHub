# BlockDocument

**패키지:** com.example.TacoHub.Document

## 개요
Notion과 같은 블록 기반 에디터의 블록 정보를 저장하는 MongoDB Document 클래스입니다. 각 블록은 텍스트, 헤더, 이미지, 테이블 등 다양한 타입을 가질 수 있으며, 계층형 구조를 통해 중첩된 블록 시스템을 지원합니다.

## 클래스 구조

### 어노테이션
- `@Document(collection = "blocks")`: MongoDB 컬렉션 "blocks"에 저장
- `@Data`: Lombok을 통한 getter/setter/toString/equals/hashCode 자동 생성
- `@Builder`: Lombok 빌더 패턴 지원
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성

### 데이터베이스 특징
- **MongoDB 사용**: 유연한 스키마와 JSON 기반 데이터 저장
- **컬렉션명**: "blocks"
- **문서 기반**: 관계형 DB의 테이블이 아닌 Document 형태로 저장

## 필드 구조

### 1. 기본 식별자
```java
@Id
@GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
@Column(name = "id", updatable = false, nullable = false)
private UUID id;
```
- **목적**: 블록의 고유 식별자
- **타입**: UUID (글로벌 고유성 보장)
- **MongoDB 필드**: `_id`

### 2. 페이지 연결
```java
@Field("page_id")
private UUID pageId;
```
- **목적**: 블록이 속한 페이지 ID
- **타입**: UUID
- **관계**: PageEntity와의 연결점
- **설계 특징**: Page와 Block 시스템 간의 브릿지

### 3. 블록 타입 및 내용
```java
@Field("block_type")
private String blockType;
```
- **목적**: 블록의 종류 정의
- **지원 타입**: 
  - `paragraph`: 일반 텍스트
  - `heading_1`, `heading_2`, `heading_3`: 제목 (H1, H2, H3)
  - `bulleted_list`: 불릿 리스트
  - `numbered_list`: 번호 리스트
  - `image`: 이미지
  - `table`: 테이블
  - 기타 확장 가능

```java
@Field("content")
private String content;
```
- **목적**: 블록의 실제 텍스트 내용
- **적용 대상**: 텍스트 기반 블록 (paragraph, heading 등)
- **비텍스트 블록**: 이미지, 테이블 등은 metadata에 구조 정보 저장

### 4. 블록 속성
```java
@Field("properties")
private Map<String, Object> properties;
```
- **목적**: 블록별 속성 및 스타일 정보
- **예시 속성**:
  - 색상: `{"color": "red"}`
  - 스타일: `{"bold": true, "italic": false}`
  - 링크: `{"url": "https://example.com"}`
  - 정렬: `{"align": "center"}`

### 5. 계층 구조
```java
@Field("parent_id")
private UUID parentId;
```
- **목적**: 부모 블록 ID (중첩 블록 지원)
- **null 의미**: 최상위 블록 (페이지 직속)
- **활용**: 들여쓰기, 중첩 리스트 등

```java
@Field("children_ids")
private List<UUID> childrenIds;
```
- **목적**: 자식 블록들의 ID 목록
- **설계 의도**: 성능 최적화를 위한 비정규화
- **활용**: 자식 블록 빠른 조회, 순서 관리

```java
@Field("has_children")
private Boolean hasChildren;
```
- **목적**: 자식 블록 존재 여부 플래그
- **활용**: UI에서 확장/축소 아이콘 표시 여부 결정
- **성능**: 별도 조회 없이 자식 존재 여부 판단

### 6. 순서 관리
```java
@Field("order_index")
private Integer orderIndex;
```
- **목적**: 같은 부모 하위에서의 블록 순서
- **시작값**: 0
- **활용**: 블록 재배열, 드래그 앤 드롭 지원

### 7. 메타데이터
```java
@Field("metadata")
private Map<String, Object> metadata;
```
- **목적**: 블록 타입별 추가 정보
- **이미지 블록**: `{"file_path": "/uploads/image.jpg", "width": 800, "height": 600}`
- **테이블 블록**: `{"rows": 3, "columns": 4, "headers": true}`
- **링크 블록**: `{"url": "https://example.com", "title": "Example"}`

### 8. 감사 정보
```java
@Field("created_at")
private LocalDateTime createdAt;

@Field("updated_at")
private LocalDateTime updatedAt;

@Field("created_by")
private String createdBy;

@Field("last_edited_by")
private String lastEditedBy;
```
- **목적**: 블록 생성/수정 이력 추적
- **created_by/last_edited_by**: 사용자 이메일 등 식별 정보
- **활용**: 협업 환경에서 편집자 추적, 이력 관리

### 9. 소프트 삭제
```java
@Field("is_deleted")
private Boolean isDeleted;
```
- **목적**: 소프트 삭제 지원
- **기본값**: false
- **장점**: 삭제 복구, 이력 보존 가능

## 메서드

### setDefaults()
**목적:** 블록 생성 시 기본값 설정

**설정 내용:**
- `hasChildren`: false
- `isDeleted`: false
- `createdAt`: 현재 시간
- `updatedAt`: 현재 시간

**활용:** 새 블록 생성 시 일관된 초기 상태 보장

### updateTimestamp()
**목적:** 블록 수정 시 updatedAt 필드 갱신

**동작:** `updatedAt`을 현재 시간으로 설정

**활용:** 블록 내용이나 속성 변경 시 호출

## MongoDB 스키마 설계

### 컬렉션 구조
```javascript
{
  "_id": ObjectId("...") | UUID,
  "page_id": UUID,
  "block_type": "paragraph",
  "content": "Hello, World!",
  "properties": {
    "color": "default",
    "bold": false
  },
  "parent_id": null,
  "order_index": 0,
  "children_ids": [],
  "has_children": false,
  "metadata": {},
  "created_at": ISODate("2024-01-01T00:00:00Z"),
  "updated_at": ISODate("2024-01-01T00:00:00Z"),
  "created_by": "user@example.com",
  "last_edited_by": "user@example.com",
  "is_deleted": false
}
```

### 인덱스 권장사항
```javascript
// 페이지별 블록 조회 최적화
db.blocks.createIndex({ "page_id": 1, "is_deleted": 1 });

// 부모-자식 관계 조회 최적화
db.blocks.createIndex({ "parent_id": 1, "order_index": 1 });

// 순서 정렬 최적화
db.blocks.createIndex({ "page_id": 1, "parent_id": 1, "order_index": 1 });

// 블록 타입별 조회
db.blocks.createIndex({ "block_type": 1 });

// 소프트 삭제 필터링
db.blocks.createIndex({ "is_deleted": 1 });
```

## 블록 타입별 구조 예시

### 텍스트 블록 (paragraph)
```json
{
  "block_type": "paragraph",
  "content": "이것은 일반 텍스트입니다.",
  "properties": {
    "color": "default",
    "bold": false,
    "italic": false
  },
  "metadata": {}
}
```

### 제목 블록 (heading_1)
```json
{
  "block_type": "heading_1",
  "content": "큰 제목",
  "properties": {
    "color": "blue",
    "bold": true
  },
  "metadata": {}
}
```

### 이미지 블록
```json
{
  "block_type": "image",
  "content": "",
  "properties": {
    "caption": "이미지 설명"
  },
  "metadata": {
    "file_path": "/uploads/2024/01/image.jpg",
    "original_name": "photo.jpg",
    "width": 800,
    "height": 600,
    "file_size": 1024000
  }
}
```

### 리스트 블록 (bulleted_list)
```json
{
  "block_type": "bulleted_list",
  "content": "첫 번째 항목",
  "properties": {
    "color": "default"
  },
  "children_ids": ["child-block-1", "child-block-2"],
  "has_children": true,
  "metadata": {}
}
```

## 계층 구조 관리

### 계층 구조 특징
- **루트 블록**: `parent_id == null`
- **자식 블록**: `parent_id != null`
- **순서 관리**: 같은 부모 하위에서 `order_index`로 정렬
- **중첩 제한**: 성능을 위해 깊이 제한 고려 필요

### 계층 조회 전략
1. **부모에서 자식으로**: `children_ids` 활용
2. **자식에서 부모로**: `parent_id` 추적
3. **같은 레벨**: `parent_id`와 `order_index` 조합

## 성능 최적화

### 비정규화 전략
- `children_ids`: 자식 목록 캐싱으로 조회 성능 향상
- `has_children`: 별도 조회 없이 자식 존재 여부 판단

### 쿼리 최적화
- 페이지별 블록 일괄 조회
- 인덱스 활용한 정렬 쿼리
- 소프트 삭제 필터링

### MongoDB 특화 최적화
- 문서 크기 최적화 (16MB 제한)
- 효율적인 임베디드 문서 활용
- 적절한 샤딩 키 선택

## 설계 이슈 및 개선 방향

### 현재 설계의 장점
1. **유연성**: MongoDB의 스키마리스 특성 활용
2. **확장성**: 새로운 블록 타입 쉽게 추가
3. **성능**: 비정규화를 통한 조회 성능 최적화

### 잠재적 문제점
1. **일관성**: children_ids와 실제 자식 블록 간 불일치 가능
2. **복잡성**: 계층 구조 변경 시 여러 필드 동기화 필요
3. **크기 제한**: 매우 깊은 계층이나 큰 메타데이터 시 문서 크기 문제

### 권장 개선 방향
1. **일관성 검증**: children_ids와 실제 자식 블록 간 주기적 검증
2. **계층 제한**: 최대 깊이 제한으로 성능 보장
3. **분리 저장**: 큰 메타데이터는 별도 컬렉션 분리 고려
4. **이벤트 소싱**: 블록 변경 이력 추적 시스템 도입

## Page-Block 연동 이슈

### 현재 연동 방식
- PageEntity의 `blockId`: 단일 블록 참조
- BlockDocument의 `pageId`: 페이지 참조

### 설계 모순점
1. **일대다 vs 일대일**: Page는 여러 Block을 가져야 하는데 단일 blockId만 참조
2. **계층 관리**: Page 계층과 Block 계층이 별도로 존재
3. **삭제 정책**: Page 삭제 시 Block 처리 방식 불명확

### 권장 해결 방안
1. **PageEntity 수정**: blockId 제거, Block 시스템에 완전 위임
2. **루트 블록 지정**: 페이지당 하나의 루트 블록으로 시작
3. **통합 계층**: Page 네비게이션과 Block 컨텐츠 계층 분리

## 사용 예시

### 기본 블록 생성
```java
BlockDocument paragraph = BlockDocument.builder()
    .pageId(pageId)
    .blockType("paragraph")
    .content("Hello, World!")
    .properties(Map.of("color", "default"))
    .parentId(null)
    .orderIndex(0)
    .build();
paragraph.setDefaults();
```

### 중첩 블록 구조
```java
// 부모 리스트 블록
BlockDocument parentList = BlockDocument.builder()
    .blockType("bulleted_list")
    .content("첫 번째 항목")
    .hasChildren(true)
    .childrenIds(Arrays.asList(childId1, childId2))
    .build();

// 자식 블록
BlockDocument childBlock = BlockDocument.builder()
    .blockType("paragraph")
    .content("중첩된 내용")
    .parentId(parentList.getId())
    .orderIndex(0)
    .build();
```

### 블록 수정
```java
blockDocument.setContent("수정된 내용");
blockDocument.updateTimestamp();
// MongoDB에 저장
```

## 연관 클래스
- **BlockService**: 블록 비즈니스 로직
- **BlockDTO**: 데이터 전송 객체
- **BlockDocumentRepository**: MongoDB 데이터 접근
- **PageEntity**: 연결된 페이지 정보

## 주의사항
1. **children_ids 동기화**: 자식 블록 추가/삭제 시 부모의 children_ids 업데이트 필수
2. **order_index 관리**: 블록 순서 변경 시 기존 블록들의 order_index 재조정
3. **순환 참조 방지**: parent_id 설정 시 순환 구조 생성 금지
4. **MongoDB 제한**: 문서 크기 16MB 제한 고려
5. **소프트 삭제**: is_deleted 필터링 누락 주의
6. **Page-Block 일관성**: pageId와 실제 페이지 존재 여부 검증 필요
