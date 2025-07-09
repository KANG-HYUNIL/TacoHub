# BlockConverter

**패키지:** com.example.TacoHub.Converter.NotionCopyConveter

## 개요
BlockDocument와 BlockDTO 간의 변환을 담당하는 유틸리티 클래스입니다. MongoDB Document와 클라이언트 간 데이터 전송용 DTO 간의 매핑을 처리하며, 서비스 로직과 변환 로직을 명확히 분리합니다.

## 클래스 구조

### 설계 원칙
- **정적 메서드**: 인스턴스 생성 없이 사용 가능한 유틸리티 클래스
- **단방향 변환**: Document ↔ DTO 양방향 변환 지원
- **null 안전성**: null 입력에 대한 안전한 처리
- **불변성**: 원본 객체 변경 없이 새 객체 생성

## 메서드 상세

### 1. `BlockDTO toDTO(BlockDocument document)`
- **목적**: MongoDB BlockDocument를 클라이언트용 BlockDTO로 변환
- **입력**: BlockDocument (MongoDB 문서)
- **출력**: BlockDTO (클라이언트 전송용)
- **특징**:
  - null 입력 시 null 반환
  - 모든 필드 매핑 (id, pageId, blockType, content 등)
  - isDeleted 필드는 DTO에 포함하지 않음 (내부 관리용)

```java
public static BlockDTO toDTO(BlockDocument document) {
    return BlockDTO.builder()
            .id(document.getId())
            .pageId(document.getPageId())
            .blockType(document.getBlockType())
            .content(document.getContent())
            .properties(document.getProperties())
            .parentId(document.getParentId())
            .orderIndex(document.getOrderIndex())
            .childrenIds(document.getChildrenIds())
            .hasChildren(document.getHasChildren())
            .metadata(document.getMetadata())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .createdBy(document.getCreatedBy())
            .lastEditedBy(document.getLastEditedBy())
            .build();
}
```

### 2. `BlockDocument toDocument(BlockDTO dto)`
- **목적**: 클라이언트 BlockDTO를 MongoDB BlockDocument로 변환
- **입력**: BlockDTO (클라이언트에서 전송)
- **출력**: BlockDocument (MongoDB 저장용)
- **특징**:
  - null 입력 시 null 반환
  - isDeleted는 기본값 false로 설정
  - 새 Document 생성 시 추가 설정 필요 (setDefaults() 호출)

```java
public static BlockDocument toDocument(BlockDTO dto) {
    return BlockDocument.builder()
            .id(dto.getId())
            .pageId(dto.getPageId())
            .blockType(dto.getBlockType())
            .content(dto.getContent())
            .properties(dto.getProperties())
            .parentId(dto.getParentId())
            .orderIndex(dto.getOrderIndex())
            .childrenIds(dto.getChildrenIds())
            .hasChildren(dto.getHasChildren())
            .metadata(dto.getMetadata())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .createdBy(dto.getCreatedBy())
            .lastEditedBy(dto.getLastEditedBy())
            .isDeleted(false) // 기본값 설정
            .build();
}
```

### 3. `List<BlockDTO> toDTOList(List<BlockDocument> documents)`
- **목적**: BlockDocument 리스트를 BlockDTO 리스트로 일괄 변환
- **입력**: List&lt;BlockDocument&gt;
- **출력**: List&lt;BlockDTO&gt;
- **특징**:
  - null 또는 빈 리스트 시 빈 리스트 반환
  - null 요소는 필터링하여 제외
  - 스트림 API 활용으로 함수형 프로그래밍 스타일

### 4. `List<BlockDocument> toDocumentList(List<BlockDTO> dtos)`
- **목적**: BlockDTO 리스트를 BlockDocument 리스트로 일괄 변환
- **입력**: List&lt;BlockDTO&gt;
- **출력**: List&lt;BlockDocument&gt;
- **특징**: toDTOList와 동일한 안전성 보장

## 사용 예시

### 서비스에서의 활용
```java
@Service
public class BlockService {
    
    // Document를 DTO로 변환하여 반환
    public BlockDTO getBlockById(UUID blockId) {
        BlockDocument document = blockDocumentRepository.findById(blockId);
        return BlockConverter.toDTO(document);
    }
    
    // 다중 블록 조회
    public List<BlockDTO> getBlocksByPageId(UUID pageId) {
        List<BlockDocument> documents = blockDocumentRepository.findByPageId(pageId);
        return BlockConverter.toDTOList(documents);
    }
}
```

## 설계 장점

### 1. **책임 분리**
- 서비스는 비즈니스 로직에 집중
- 변환 로직은 Converter에서 전담
- 코드 가독성 및 유지보수성 향상

### 2. **재사용성**
- 여러 서비스에서 공통 변환 로직 활용
- 일관된 변환 규칙 적용
- 중복 코드 제거

### 3. **테스트 용이성**
- 변환 로직만 독립적으로 테스트 가능
- 서비스 테스트에서 변환 로직 분리
- Mock 객체 생성 시 활용 가능

### 4. **확장성**
- 새로운 변환 규칙 추가 용이
- 특별한 변환이 필요한 경우 오버로드 메서드 추가
- 다른 도메인의 Converter 패턴 적용 가능

## 연관 클래스
- **BlockDocument**: MongoDB 문서 엔티티
- **BlockDTO**: 클라이언트 전송용 DTO
- **BlockService**: 주요 사용처인 비즈니스 서비스
- **PageConverter**: 유사한 변환 패턴을 가진 페이지 컨버터

## 주의사항
1. **null 안전성**: 항상 null 체크 수행
2. **불변성**: 원본 객체 수정 없이 새 객체 생성
3. **필드 누락**: 새 필드 추가 시 변환 메서드에도 반영 필요
4. **타입 안전성**: 제네릭을 활용한 타입 안전성 보장
