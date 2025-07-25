# BlockDTO

**패키지:** com.example.TacoHub.Dto.NotionCopyDTO

## 개요
블록 정보 전송을 위한 DTO(Data Transfer Object) 클래스입니다. Notion과 같은 블록 기반 에디터의 블록 데이터를 클라이언트와 서버 간에 주고받기 위한 표준화된 데이터 구조를 제공합니다.

## 클래스 구조

### 어노테이션
- `@Data`: Lombok을 통한 getter/setter/toString/equals/hashCode 자동 생성
- `@Builder`: Lombok 빌더 패턴 지원
- `@NoArgsConstructor`, `@AllArgsConstructor`: 기본/전체 생성자 자동 생성

### 특징
- **불변성**: DTO는 데이터 전송 목적으로 설계, 비즈니스 로직 미포함
- **직렬화**: JSON 직렬화/역직렬화 지원
- **타입 안전성**: 강타입 언어의 장점 활용

## 필드 구조

### 1. 기본 식별자
```java
private UUID id;
```
- **목적**: 블록의 고유 식별자
- **타입**: UUID
- **null 허용**: 새 블록 생성 시에는 null, 서버에서 자동 생성

### 2. 페이지 연결
```java
private UUID pageId;
```
- **목적**: 블록이 속한 페이지 ID
- **필수성**: 필수 필드 (모든 블록은 특정 페이지에 소속)
- **관계**: PageEntity/PageDTO와의 연결점

### 3. 블록 타입 및 내용
```java
private String blockType;
```
- **목적**: 블록의 종류 정의
- **지원 타입**:
  - `paragraph`: 일반 텍스트 단락
  - `heading_1`, `heading_2`, `heading_3`: 제목 레벨 1-3
  - `bulleted_list`: 불릿 리스트
  - `numbered_list`: 번호 리스트
  - `image`: 이미지 블록
  - `table`: 테이블 (확장 예정)
  - 기타 확장 가능한 구조
# BlockDTO

<table>
  <tr><th>패키지</th><td>com.example.TacoHub.Dto.NotionCopyDTO</td></tr>
  <tr><th>클래스 설명</th><td>블록 정보 전송을 위한 DTO(Data Transfer Object) 클래스.<br>Notion과 같은 블록 기반 에디터의 블록 데이터를 클라이언트와 서버 간에 주고받기 위한 표준화된 데이터 구조를 제공한다.</td></tr>
</table>

## 필드 상세 (Fields)
<table>
  <tr><th>이름</th><th>타입</th><th>설명</th><th>예시/제약</th></tr>
  <tr><td>id</td><td>UUID</td><td>블록의 고유 식별자. 서버에서 자동 생성.</td><td>"b1a2c3d4-..."</td></tr>
  <tr><td>pageId</td><td>UUID</td><td>블록이 속한 페이지의 ID. 필수.</td><td>"p1a2c3d4-..."</td></tr>
  <tr><td>blockType</td><td>String</td><td>블록의 종류(paragraph, heading_1, bulleted_list 등).</td><td>"paragraph", "heading_1", "image" 등</td></tr>
  <tr><td>content</td><td>String</td><td>블록의 실제 내용(텍스트, 이미지 URL 등).</td><td>"회의록 내용", "https://.../image.png"</td></tr>
  <tr><td>orderIndex</td><td>Integer</td><td>페이지 내 블록의 순서 인덱스.</td><td>0, 1, 2...</td></tr>
  <tr><td>createdAt</td><td>LocalDateTime</td><td>블록 생성 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
  <tr><td>updatedAt</td><td>LocalDateTime</td><td>블록 수정 일시.</td><td>"2024-01-15T10:30:00"</td></tr>
</table>

## 생성자 (Constructors)
<table>
  <tr><th>생성자</th><th>설명</th></tr>
  <tr><td>BlockDTO()</td><td>기본 생성자. Lombok 또는 명시적 생성자 사용 가능.</td></tr>
  <tr><td>BlockDTO(id, pageId, blockType, content, orderIndex, createdAt, updatedAt)</td><td>모든 필드를 초기화하는 생성자.</td></tr>
</table>

## 메서드 상세 (Methods)
<table>
  <tr><th>메서드</th><th>설명</th><th>매개변수</th><th>반환값</th></tr>
  <tr>
    <td>getter/setter</td>
    <td>각 필드의 값을 조회/설정하는 메서드. Lombok @Data로 자동 생성.</td>
    <td>각 필드별(UUID id, ...)</td>
    <td>해당 필드 값</td>
  </tr>
</table>

## 상속 관계 (Inheritance)
<table>
  <tr><th>부모 클래스</th><th>설명</th></tr>
  <tr><td>BaseDateDTO</td><td>생성일시(createdAt), 수정일시(updatedAt) 정보 포함.</td></tr>
</table>

## 동작 흐름 (Lifecycle)
1. 클라이언트/서버 간 블록 데이터 전송 시 BlockDTO 객체가 생성된다.
2. 각 필드에 값이 할당되어 블록 정보가 전달된다.
3. Entity 변환, DB 저장, 비즈니스 로직 처리 등에 활용된다.

## 활용 예시 (Usage)
블록 생성 요청:
```json
{
  "pageId": "p1a2c3d4-...",
  "blockType": "paragraph",
  "content": "회의록 내용",
  "orderIndex": 0
}
```
블록 조회 응답:
```json
{
  "id": "b1a2c3d4-...",
  "pageId": "p1a2c3d4-...",
  "blockType": "heading_1",
  "content": "회의 제목",
  "orderIndex": 1,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 예외 및 주의사항 (Exceptions & Notes)
- pageId, blockType 등 필수값 누락 시 API 요청이 거부될 수 있음.
- content 필드는 블록 타입에 따라 형식이 달라질 수 있음.
- createdAt, updatedAt은 서버에서 자동 관리됨.
- **시작값**: 0
- **증가**: 1씩 증가
- **활용**: 드래그 앤 드롭으로 블록 재배열

### 7. 메타데이터
```java
private Map<String, Object> metadata;
```
- **목적**: 블록 타입별 추가 정보
- **유연성**: 새로운 블록 타입 확장 시 구조 변경 없이 대응

**타입별 메타데이터 예시:**

**이미지 블록:**
```json
{
  "url": "https://example.com/image.jpg",
  "caption": "이미지 설명",
  "width": 800,
  "height": 600,
  "file_size": 1024000
}
```

**테이블 블록:**
```json
{
  "rows": 3,
  "columns": 4,
  "headers": true,
  "data": [
    ["헤더1", "헤더2", "헤더3"],
    ["데이터1", "데이터2", "데이터3"]
  ]
}
```

### 8. 감사 정보
```java
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private String createdBy;
private String lastEditedBy;
```
- **목적**: 블록 생성/수정 이력 추적
- **createdBy/lastEditedBy**: 사용자 식별 정보 (이메일, ID 등)
- **활용**: 협업 환경에서 편집자 표시, 이력 관리

## 정적 팩토리 메서드

### 1. createTextBlock()
**목적:** 간단한 텍스트 블록 생성

**매개변수:**
- `pageId`: 페이지 ID (필수)
- `content`: 텍스트 내용
- `orderIndex`: 순서

**반환값:** 텍스트 블록 DTO

**예시:**
```java
BlockDTO textBlock = BlockDTO.createTextBlock(
    pageId, 
    "안녕하세요!", 
    0
);
```

### 2. createHeadingBlock()
**목적:** 헤더 블록 생성

**매개변수:**
- `pageId`: 페이지 ID (필수)
- `content`: 헤더 텍스트
- `level`: 헤더 레벨 (1, 2, 3)
- `orderIndex`: 순서

**반환값:** 헤더 블록 DTO

**예시:**
```java
BlockDTO h1Block = BlockDTO.createHeadingBlock(
    pageId, 
    "큰 제목", 
    1, 
    0
);
```

### 3. createListBlock()
**목적:** 리스트 블록 생성

**매개변수:**
- `pageId`: 페이지 ID (필수)
- `content`: 리스트 아이템 텍스트
- `isNumbered`: 번호 매김 여부 (true: numbered_list, false: bulleted_list)
- `orderIndex`: 순서

**반환값:** 리스트 블록 DTO

**예시:**
```java
BlockDTO bulletList = BlockDTO.createListBlock(
    pageId, 
    "첫 번째 항목", 
    false, 
    0
);
```

### 4. createImageBlock()
**목적:** 이미지 블록 생성

**매개변수:**
- `pageId`: 페이지 ID (필수)
- `imageUrl`: 이미지 URL
- `caption`: 이미지 캡션 (선택)
- `orderIndex`: 순서

**반환값:** 이미지 블록 DTO

**메타데이터 자동 설정:**
- `url`: 이미지 URL
- `caption`: 캡션 (null인 경우 빈 문자열)

**예시:**
```java
BlockDTO imageBlock = BlockDTO.createImageBlock(
    pageId, 
    "https://example.com/image.jpg", 
    "멋진 풍경", 
    0
);
```

## 블록 타입별 구조

### 텍스트 블록 (paragraph)
```json
{
  "blockType": "paragraph",
  "content": "일반 텍스트 내용",
  "properties": {
    "color": "default",
    "bold": false
  },
  "hasChildren": false
}
```

### 헤더 블록 (heading_1)
```json
{
  "blockType": "heading_1",
  "content": "큰 제목",
  "properties": {
    "color": "blue",
    "bold": true
  },
  "hasChildren": false
}
```

### 리스트 블록 (bulleted_list)
```json
{
  "blockType": "bulleted_list",
  "content": "첫 번째 항목",
  "childrenIds": ["child-id-1", "child-id-2"],
  "hasChildren": true,
  "orderIndex": 0
}
```

### 이미지 블록 (image)
```json
{
  "blockType": "image",
  "content": "",
  "metadata": {
    "url": "https://example.com/image.jpg",
    "caption": "이미지 설명"
  },
  "hasChildren": false
}
```

## JSON 직렬화 예시

### 완전한 BlockDTO JSON
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "pageId": "987fcdeb-51a2-43d7-8b9f-123456789abc",
  "blockType": "paragraph",
  "content": "이것은 샘플 텍스트입니다.",
  "properties": {
    "color": "default",
    "bold": false,
    "italic": true
  },
  "parentId": null,
  "orderIndex": 0,
  "childrenIds": [],
  "hasChildren": false,
  "metadata": {},
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "createdBy": "user@example.com",
  "lastEditedBy": "user@example.com"
}
```

## 변환 및 검증

### Entity ↔ DTO 변환
- **BlockDocument → BlockDTO**: 서버에서 클라이언트로 데이터 전송
- **BlockDTO → BlockDocument**: 클라이언트에서 서버로 데이터 수신
- **변환 도구**: Converter 클래스 또는 MapStruct 활용

### 입력값 검증
```java
// Controller 레벨에서의 검증 예시
@Valid
public ResponseEntity<BlockDTO> createBlock(@RequestBody @Valid BlockDTO blockDTO) {
    // pageId 필수 검증
    if (blockDTO.getPageId() == null) {
        throw new IllegalArgumentException("Page ID is required");
    }
    
    // blockType 검증
    if (!isValidBlockType(blockDTO.getBlockType())) {
        throw new IllegalArgumentException("Invalid block type");
    }
    
    return ResponseEntity.ok(blockService.createBlock(blockDTO));
}
```

## 프론트엔드 연동

### TypeScript 인터페이스
```typescript
interface BlockDTO {
  id?: string;
  pageId: string;
  blockType: string;
  content?: string;
  properties?: Record<string, any>;
  parentId?: string;
  orderIndex: number;
  childrenIds?: string[];
  hasChildren?: boolean;
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  lastEditedBy?: string;
}
```

### REST API 예시
```javascript
// 블록 생성
const createBlock = async (blockData) => {
  const response = await fetch('/api/blocks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(blockData)
  });
  return response.json();
};

// 블록 조회
const getBlocks = async (pageId) => {
  const response = await fetch(`/api/blocks?pageId=${pageId}`);
  return response.json();
};
```

## 성능 고려사항

### 직렬화 최적화
- 불필요한 필드 제외 (Jackson `@JsonIgnore`)
- 지연 로딩 필드 처리
- 순환 참조 방지

### 페이징 처리
```java
// 대량 블록 조회 시 페이징
public Page<BlockDTO> getBlocks(UUID pageId, Pageable pageable) {
    Page<BlockDocument> blocks = blockRepository.findByPageId(pageId, pageable);
    return blocks.map(BlockConverter::toDTO);
}
```

### 캐싱 전략
- 자주 조회되는 블록 DTO 캐싱
- 페이지별 블록 목록 캐싱
- 캐시 무효화 전략

## 확장 계획

### 새로운 블록 타입 추가
```java
// 코드 블록 팩토리 메서드 예시
public static BlockDTO createCodeBlock(UUID pageId, String code, String language, Integer orderIndex) {
    Map<String, Object> metadata = Map.of(
        "language", language,
        "syntax_highlighting", true
    );
    
    return BlockDTO.builder()
        .pageId(pageId)
        .blockType("code")
        .content(code)
        .metadata(metadata)
        .orderIndex(orderIndex)
        .hasChildren(false)
        .build();
}
```

### 검증 어노테이션 추가
```java
@Valid
@NotNull(message = "Page ID is required")
private UUID pageId;

@NotBlank(message = "Block type is required")
@Pattern(regexp = "^(paragraph|heading_[123]|bulleted_list|numbered_list|image)$", 
         message = "Invalid block type")
private String blockType;

@Min(value = 0, message = "Order index must be non-negative")
private Integer orderIndex;
```

## 사용 예시

### 기본 블록 생성
```java
// 텍스트 블록
BlockDTO textBlock = BlockDTO.createTextBlock(
    pageId, 
    "안녕하세요!", 
    0
);

// 헤더 블록
BlockDTO headerBlock = BlockDTO.createHeadingBlock(
    pageId, 
    "제목", 
    1, 
    1
);

// 이미지 블록
BlockDTO imageBlock = BlockDTO.createImageBlock(
    pageId, 
    "https://example.com/image.jpg", 
    "설명", 
    2
);
```

### 계층형 블록 구조
```java
// 부모 리스트 블록
BlockDTO parentList = BlockDTO.builder()
    .pageId(pageId)
    .blockType("bulleted_list")
    .content("첫 번째 항목")
    .orderIndex(0)
    .hasChildren(true)
    .childrenIds(Arrays.asList(childId1, childId2))
    .build();

// 자식 블록
BlockDTO childBlock = BlockDTO.builder()
    .pageId(pageId)
    .blockType("paragraph")
    .content("중첩된 내용")
    .parentId(parentList.getId())
    .orderIndex(0)
    .hasChildren(false)
    .build();
```

## 연관 클래스
- **BlockDocument**: MongoDB 저장 엔티티
- **BlockService**: 비즈니스 로직 처리
- **BlockController**: REST API 엔드포인트
- **BlockConverter**: Entity ↔ DTO 변환
- **PageDTO**: 관련 페이지 정보

## 주의사항
1. **필수 필드**: pageId, blockType, orderIndex는 반드시 설정
2. **타입 안전성**: blockType은 지원되는 타입만 사용
3. **계층 일관성**: parentId와 childrenIds의 양방향 일관성 유지
4. **순서 관리**: orderIndex는 같은 부모 하위에서 고유해야 함
5. **메타데이터**: 블록 타입에 맞는 메타데이터 구조 사용
6. **null 처리**: 선택적 필드의 null 처리 주의
7. **JSON 크기**: 대량 메타데이터로 인한 JSON 크기 주의
