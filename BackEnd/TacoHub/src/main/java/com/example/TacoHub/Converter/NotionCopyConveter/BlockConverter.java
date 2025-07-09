package com.example.TacoHub.Converter.NotionCopyConveter;

import com.example.TacoHub.Document.BlockDocument;
import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BlockDocument와 BlockDTO 간의 변환을 담당하는 Converter 클래스
 */
public class BlockConverter {

    /**
     * BlockDocument를 BlockDTO로 변환합니다
     * @param document 변환할 Document
     * @return 변환된 DTO
     */
    public static BlockDTO toDTO(BlockDocument document) {
        if (document == null) {
            return null;
        }

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

    /**
     * BlockDTO를 BlockDocument로 변환합니다
     * @param dto 변환할 DTO
     * @return 변환된 Document
     */
    public static BlockDocument toDocument(BlockDTO dto) {
        if (dto == null) {
            return null;
        }

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
                .isDeleted(false)
                .build();
    }

    /**
     * BlockDocument 리스트를 BlockDTO 리스트로 변환합니다
     * @param documents 변환할 Document 리스트
     * @return 변환된 DTO 리스트
     */
    public static List<BlockDTO> toDTOList(List<BlockDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        return documents.stream()
                .map(BlockConverter::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * BlockDTO 리스트를 BlockDocument 리스트로 변환합니다
     * @param dtos 변환할 DTO 리스트
     * @return 변환된 Document 리스트
     */
    public static List<BlockDocument> toDocumentList(List<BlockDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(BlockConverter::toDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
