package com.example.TacoHub.Document;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Notion과 같은 블록 기반 에디터의 블록 정보를 저장하는 MongoDB Document
 * 각 블록은 텍스트, 헤더, 이미지, 테이블 등 다양한 타입을 가질 수 있음
 */
@Document(collection = "blocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDocument {

    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // 블록 고유 ID

    @Field("page_id")
    private UUID pageId; // 블록이 속한 페이지 ID

    @Field("block_type")
    private String blockType; // 블록 타입 (paragraph, heading_1, heading_2, heading_3, bulleted_list, numbered_list, image, etc.)

    @Field("content")
    private String content; // 블록의 텍스트 내용 (텍스트 블록의 경우)

    @Field("properties")
    private Map<String, Object> properties; // 블록별 속성 (색상, 스타일, 링크 등)

    @Field("parent_id")
    private UUID parentId; // 부모 블록 ID (중첩 블록의 경우, null이면 최상위 블록)

    @Field("order_index")
    private Integer orderIndex; // 같은 부모 아래에서의 순서 (0부터 시작)

    @Field("children_ids")
    private List<UUID> childrenIds; // 자식 블록들의 ID 목록 (성능을 위한 비정규화)

    @Field("has_children")
    private Boolean hasChildren; // 자식 블록 존재 여부 (빠른 판단을 위함)

    @Field("metadata")
    private Map<String, Object> metadata; // 추가 메타데이터 (파일 경로, 이미지 URL, 테이블 구조 등)

    @Field("created_at")
    private LocalDateTime createdAt; // 블록 생성 시간

    @Field("updated_at")
    private LocalDateTime updatedAt; // 블록 최종 수정 시간

    @Field("created_by")
    private String createdBy; // 블록 생성자 (사용자 이메일 등)

    @Field("last_edited_by")
    private String lastEditedBy; // 최종 편집자 (사용자 이메일 등)

    @Field("is_deleted")
    private Boolean isDeleted; // 삭제 여부 (소프트 삭제)

    /**
     * 블록 생성 시 기본값 설정
     */
    public void setDefaults() {
        if (this.hasChildren == null) {
            this.hasChildren = false;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 블록 수정 시 updatedAt 갱신
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

