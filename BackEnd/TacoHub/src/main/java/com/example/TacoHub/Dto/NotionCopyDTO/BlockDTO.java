package com.example.TacoHub.Dto.NotionCopyDTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 블록 정보 전송을 위한 DTO 클래스
 * Notion과 같은 블록 기반 에디터의 블록 데이터를 클라이언트와 주고받기 위함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDTO {

    private UUID id; // 블록 고유 ID
    
    private UUID pageId; // 블록이 속한 페이지 ID
    
    private String blockType; // 블록 타입 (paragraph, heading_1, heading_2, heading_3, bulleted_list, numbered_list, image, etc.)
    
    private String content; // 블록의 텍스트 내용 (텍스트 블록의 경우)
    
    private Map<String, Object> properties; // 블록별 속성 (색상, 스타일, 링크 등)
    
    private UUID parentId; // 부모 블록 ID (중첩 블록의 경우, null이면 최상위 블록)
    
    private Integer orderIndex; // 같은 부모 아래에서의 순서 (0부터 시작)
    
    private List<UUID> childrenIds; // 자식 블록들의 ID 목록
    
    private Boolean hasChildren; // 자식 블록 존재 여부
    
    private Map<String, Object> metadata; // 추가 메타데이터 (파일 경로, 이미지 URL, 테이블 구조 등)
    
    private LocalDateTime createdAt; // 블록 생성 시간
    
    private LocalDateTime updatedAt; // 블록 최종 수정 시간
    
    private String createdBy; // 블록 생성자 (사용자 이메일 등)
    
    private String lastEditedBy; // 최종 편집자 (사용자 이메일 등)

    /**
     * 간단한 텍스트 블록 생성을 위한 정적 팩토리 메서드
     * @param pageId 페이지 ID
     * @param content 텍스트 내용
     * @param orderIndex 순서
     * @return 텍스트 블록 DTO
     */
    public static BlockDTO createTextBlock(UUID pageId, String content, Integer orderIndex) {
        return BlockDTO.builder()
                .pageId(pageId)
                .blockType("paragraph")
                .content(content)
                .orderIndex(orderIndex)
                .hasChildren(false)
                .build();
    }

    /**
     * 헤더 블록 생성을 위한 정적 팩토리 메서드
     * @param pageId 페이지 ID
     * @param content 헤더 텍스트
     * @param level 헤더 레벨 (1, 2, 3)
     * @param orderIndex 순서
     * @return 헤더 블록 DTO
     */
    public static BlockDTO createHeadingBlock(UUID pageId, String content, int level, Integer orderIndex) {
        String blockType = "heading_" + level;
        return BlockDTO.builder()
                .pageId(pageId)
                .blockType(blockType)
                .content(content)
                .orderIndex(orderIndex)
                .hasChildren(false)
                .build();
    }

    /**
     * 리스트 블록 생성을 위한 정적 팩토리 메서드
     * @param pageId 페이지 ID
     * @param content 리스트 아이템 텍스트
     * @param isNumbered 번호 매김 여부 (true: numbered_list, false: bulleted_list)
     * @param orderIndex 순서
     * @return 리스트 블록 DTO
     */
    public static BlockDTO createListBlock(UUID pageId, String content, boolean isNumbered, Integer orderIndex) {
        String blockType = isNumbered ? "numbered_list" : "bulleted_list";
        return BlockDTO.builder()
                .pageId(pageId)
                .blockType(blockType)
                .content(content)
                .orderIndex(orderIndex)
                .hasChildren(false)
                .build();
    }

    /**
     * 이미지 블록 생성을 위한 정적 팩토리 메서드
     * @param pageId 페이지 ID
     * @param imageUrl 이미지 URL
     * @param caption 이미지 캡션
     * @param orderIndex 순서
     * @return 이미지 블록 DTO
     */
    public static BlockDTO createImageBlock(UUID pageId, String imageUrl, String caption, Integer orderIndex) {
        Map<String, Object> metadata = Map.of(
                "url", imageUrl,
                "caption", caption != null ? caption : ""
        );
        
        return BlockDTO.builder()
                .pageId(pageId)
                .blockType("image")
                .metadata(metadata)
                .orderIndex(orderIndex)
                .hasChildren(false)
                .build();
    }
}
