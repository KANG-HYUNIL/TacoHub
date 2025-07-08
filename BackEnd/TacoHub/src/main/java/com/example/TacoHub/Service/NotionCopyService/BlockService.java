package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.TacoHub.Document.BlockDocument;
import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Exception.NotionCopyException.BlockNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.BlockOperationException;
import com.example.TacoHub.Repository.NotionCopyRepository.BlockDocumentRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 블록 관련 비즈니스 로직을 처리하는 서비스 클래스
 * Notion과 같은 블록 기반 에디터의 블록 CRUD 및 관리 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService {

    // MongoDB Repository는 추후 생성 예정
    private final BlockDocumentRepository blockDocumentRepository;

    /**
     * 새로운 블록을 생성합니다
     * @param blockDTO 생성할 블록 정보
     * @return 생성된 블록 DTO
     */
    @Transactional
    public BlockDTO createBlock(BlockDTO blockDTO) {
        try {
            log.info("블록 생성 시작: pageId={}, blockType={}", 
                    blockDTO.getPageId(), blockDTO.getBlockType());

            // 입력값 검증
            if (blockDTO.getPageId() == null) {
                throw new BlockOperationException("페이지 ID는 필수입니다");
            }
            if (blockDTO.getBlockType() == null || blockDTO.getBlockType().trim().isEmpty()) {
                throw new BlockOperationException("블록 타입은 필수입니다");
            }

            // DTO를 Document로 변환
            BlockDocument blockDocument = convertToDocument(blockDTO);
            
            // 기본값 설정
            blockDocument.setId(UUID.randomUUID());
            blockDocument.setDefaults();

            // 순서 인덱스 자동 설정 (페이지 내 마지막 블록 다음 순서)
            if (blockDocument.getOrderIndex() == null) {
                blockDocument.setOrderIndex(getNextOrderIndex(blockDTO.getPageId(), blockDTO.getParentId()));
            }

            // 부모 블록 검증 (부모 ID가 있는 경우)
            if (blockDocument.getParentId() != null) {
                BlockDocument parentBlock = getBlockDocumentOrThrow(blockDocument.getParentId());
                if (!parentBlock.getPageId().equals(blockDocument.getPageId())) {
                    throw new BlockOperationException("부모 블록과 같은 페이지에만 자식 블록을 생성할 수 있습니다");
                }
            }

            // MongoDB에 저장
            BlockDocument savedDocument = blockDocumentRepository.save(blockDocument);

            // 부모 블록이 있다면 부모의 hasChildren을 true로 업데이트
            if (savedDocument.getParentId() != null) {
                updateParentHasChildren(savedDocument.getParentId(), true);
                updateParentChildrenList(savedDocument.getParentId(), savedDocument.getId(), true);
            }

            log.info("블록 생성 완료: blockId={}, pageId={}", savedDocument.getId(), savedDocument.getPageId());
            return convertToDTO(savedDocument);

        } catch (BlockOperationException | BlockNotFoundException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("블록 생성 비즈니스 오류: pageId={}, blockType={}, error={}", 
                    blockDTO.getPageId(), blockDTO.getBlockType(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            log.error("블록 생성 시스템 오류: pageId={}, blockType={}, error={}", 
                    blockDTO.getPageId(), blockDTO.getBlockType(), e.getMessage(), e);
            handleAndThrowBlockException("createBlock", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 블록 내용을 수정합니다
     * @param blockId 수정할 블록 ID
     * @param content 새로운 내용
     * @param properties 새로운 속성
     * @return 수정된 블록 DTO
     */
    @Transactional
    public BlockDTO updateBlockContent(UUID blockId, String content, Map<String, Object> properties) {
        try {
            log.info("블록 내용 수정 시작: blockId={}", blockId);

            // 입력값 검증
            if (blockId == null) {
                throw new BlockOperationException("블록 ID는 필수입니다");
            }

            // Repository에서 블록 조회 (존재하지 않으면 예외 발생)
            BlockDocument blockDocument = getBlockDocumentOrThrow(blockId);

            // 내용 및 속성 업데이트
            if (content != null) {
                blockDocument.setContent(content);
            }
            if (properties != null) {
                blockDocument.setProperties(properties);
            }
            
            blockDocument.updateTimestamp();

            // MongoDB에 저장
            BlockDocument savedDocument = blockDocumentRepository.save(blockDocument);

            log.info("블록 내용 수정 완료: blockId={}", blockId);
            return convertToDTO(savedDocument);

        } catch (BlockOperationException | BlockNotFoundException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("블록 내용 수정 비즈니스 오류: blockId={}, error={}", blockId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            log.error("블록 내용 수정 시스템 오류: blockId={}, error={}", blockId, e.getMessage(), e);
            handleAndThrowBlockException("updateBlockContent", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 블록의 순서를 변경합니다
     * @param blockId 이동할 블록 ID
     * @param newParentId 새로운 부모 블록 ID (null이면 최상위)
     * @param newOrderIndex 새로운 순서 인덱스
     * @return 이동된 블록 DTO
     */
    @Transactional
    public BlockDTO moveBlock(UUID blockId, UUID newParentId, Integer newOrderIndex) {
        try {
            log.info("블록 이동 시작: blockId={}, newParentId={}, newOrderIndex={}", 
                    blockId, newParentId, newOrderIndex);

            // 입력값 검증
            if (blockId == null) {
                throw new BlockOperationException("블록 ID는 필수입니다");
            }
            if (newOrderIndex == null || newOrderIndex < 0) {
                throw new BlockOperationException("순서 인덱스는 0 이상이어야 합니다");
            }

            // Repository에서 블록 조회 (존재하지 않으면 예외 발생)
            BlockDocument blockDocument = getBlockDocumentOrThrow(blockId);

            // 자기 자신을 부모로 설정하는 것 방지
            if (newParentId != null && newParentId.equals(blockId)) {
                throw new BlockOperationException("블록은 자기 자신을 부모로 가질 수 없습니다");
            }

            // 새로운 부모 블록 검증 (부모 ID가 있는 경우)
            if (newParentId != null) {
                BlockDocument newParentBlock = getBlockDocumentOrThrow(newParentId);
                if (!newParentBlock.getPageId().equals(blockDocument.getPageId())) {
                    throw new BlockOperationException("같은 페이지 내에서만 블록을 이동할 수 있습니다");
                }
            }

            UUID oldParentId = blockDocument.getParentId();

            // 블록 위치 업데이트
            blockDocument.setParentId(newParentId);
            blockDocument.setOrderIndex(newOrderIndex);
            blockDocument.updateTimestamp();

            // MongoDB에 저장
            BlockDocument savedDocument = blockDocumentRepository.save(blockDocument);

            // 기존 부모의 자식 목록에서 제거 및 hasChildren 업데이트
            if (oldParentId != null) {
                updateParentChildrenList(oldParentId, blockId, false);
                updateParentHasChildrenIfNeeded(oldParentId);
            }

            // 새로운 부모의 자식 목록에 추가 및 hasChildren 업데이트
            if (newParentId != null) {
                updateParentChildrenList(newParentId, blockId, true);
                updateParentHasChildren(newParentId, true);
            }

            log.info("블록 이동 완료: blockId={}", blockId);
            return convertToDTO(savedDocument);

        } catch (BlockOperationException | BlockNotFoundException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("블록 이동 비즈니스 오류: blockId={}, newParentId={}, error={}", 
                    blockId, newParentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            log.error("블록 이동 시스템 오류: blockId={}, newParentId={}, error={}", 
                    blockId, newParentId, e.getMessage(), e);
            handleAndThrowBlockException("moveBlock", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 블록을 삭제합니다 (소프트 삭제)
     * @param blockId 삭제할 블록 ID
     */
    @Transactional
    public void deleteBlock(UUID blockId) {
        try {
            log.info("블록 삭제 시작: blockId={}", blockId);

            // 입력값 검증
            if (blockId == null) {
                throw new BlockOperationException("블록 ID는 필수입니다");
            }

            // Repository에서 블록 조회 (존재하지 않으면 예외 발생)
            BlockDocument blockDocument = getBlockDocumentOrThrow(blockId);

            // 자식 블록들도 함께 삭제 (재귀적 삭제)
            if (blockDocument.getHasChildren() && blockDocument.getChildrenIds() != null) {
                for (UUID childId : blockDocument.getChildrenIds()) {
                    deleteBlock(childId);
                }
            }

            // 소프트 삭제 처리
            blockDocument.setIsDeleted(true);
            blockDocument.updateTimestamp();

            // MongoDB에 저장
            blockDocumentRepository.save(blockDocument);

            // 부모 블록의 자식 목록에서 제거
            if (blockDocument.getParentId() != null) {
                updateParentChildrenList(blockDocument.getParentId(), blockId, false);
                updateParentHasChildrenIfNeeded(blockDocument.getParentId());
            }

            log.info("블록 삭제 완료: blockId={}", blockId);

        } catch (BlockOperationException | BlockNotFoundException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("블록 삭제 비즈니스 오류: blockId={}, error={}", blockId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            log.error("블록 삭제 시스템 오류: blockId={}, error={}", blockId, e.getMessage(), e);
            handleAndThrowBlockException("deleteBlock", e);
        }
    }

    /**
     * 페이지의 모든 블록을 조회합니다
     * @param pageId 페이지 ID
     * @return 블록 DTO 목록 (계층 구조 유지)
     */
    public List<BlockDTO> getBlocksByPageId(UUID pageId) {
        try {
            log.info("페이지 블록 조회 시작: pageId={}", pageId);

            // Repository에서 페이지의 모든 블록 조회 (삭제되지 않은 것만)
            List<BlockDocument> blocks = blockDocumentRepository.findByPageIdAndIsDeletedOrderByOrderIndex(pageId, false);

            // 계층 구조로 정렬된 블록 목록 반환
            List<BlockDTO> sortedBlocks = buildHierarchicalBlockList(blocks);

            log.info("페이지 블록 조회 완료: pageId={}, 블록 수={}", pageId, sortedBlocks.size());
            return sortedBlocks;

        } catch (Exception e) {
            handleAndThrowBlockException("getBlocksByPageId", e);
            return Collections.emptyList(); // 실제로는 도달하지 않음
        }
    }

    /**
     * 특정 블록을 ID로 조회합니다
     * @param blockId 블록 ID
     * @return 블록 DTO
     */
    public BlockDTO getBlockById(UUID blockId) {
        try {
            log.info("블록 조회 시작: blockId={}", blockId);

            // Repository에서 블록 조회
            BlockDocument blockDocument = getBlockDocumentOrThrow(blockId);

            log.info("블록 조회 완료: blockId={}", blockId);
            return convertToDTO(blockDocument);

        } catch (Exception e) {
            handleAndThrowBlockException("getBlockById", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    // === 내부 헬퍼 메서드들 ===

    /**
     * 다음 순서 인덱스를 계산합니다
     * @param pageId 페이지 ID
     * @param parentId 부모 블록 ID
     * @return 다음 순서 인덱스
     */
    private Integer getNextOrderIndex(UUID pageId, UUID parentId) {
        // 해당 페이지와 부모에서 가장 큰 orderIndex를 찾아서 +1
        List<BlockDocument> existingBlocks = blockDocumentRepository.findByPageIdAndParentIdAndIsDeleted(pageId, parentId, false);
        
        return existingBlocks.stream()
                .mapToInt(BlockDocument::getOrderIndex)
                .max()
                .orElse(-1) + 1;
    }

    /**
     * 블록을 ID로 조회하고, 없으면 예외를 던지는 메서드
     * @param blockId 조회할 블록 ID
     * @return 조회된 BlockDocument
     * @throws BlockNotFoundException 블록이 존재하지 않을 때
     */
    private BlockDocument getBlockDocumentOrThrow(UUID blockId) {
        return blockDocumentRepository.findById(blockId)
                .filter(block -> !block.getIsDeleted()) // 삭제되지 않은 블록만
                .orElseThrow(() -> {
                    log.warn("블록 조회 실패: ID가 존재하지 않음, blockId={}", blockId);
                    return new BlockNotFoundException("블록이 존재하지 않습니다: " + blockId);
                });
    }

    /**
     * 부모 블록의 hasChildren 플래그를 업데이트합니다
     * @param parentId 부모 블록 ID
     * @param hasChildren 자식 존재 여부
     */
    private void updateParentHasChildren(UUID parentId, boolean hasChildren) {
        try {
            BlockDocument parentBlock = getBlockDocumentOrThrow(parentId);
            parentBlock.setHasChildren(hasChildren);
            parentBlock.updateTimestamp();
            blockDocumentRepository.save(parentBlock);
            
            log.debug("부모 블록 hasChildren 업데이트: parentId={}, hasChildren={}", parentId, hasChildren);
        } catch (Exception e) {
            log.warn("부모 블록 hasChildren 업데이트 실패: parentId={}, error={}", parentId, e.getMessage());
        }
    }

    /**
     * 부모 블록의 hasChildren 플래그를 필요시 업데이트합니다 (자식이 있는지 확인 후)
     * @param parentId 부모 블록 ID
     */
    private void updateParentHasChildrenIfNeeded(UUID parentId) {
        try {
            List<BlockDocument> children = blockDocumentRepository.findByParentIdAndIsDeletedOrderByOrderIndex(parentId, false);
            boolean hasChildren = !children.isEmpty();
            updateParentHasChildren(parentId, hasChildren);
        } catch (Exception e) {
            log.warn("부모 블록 hasChildren 자동 업데이트 실패: parentId={}, error={}", parentId, e.getMessage());
        }
    }

    /**
     * 부모 블록의 자식 목록을 업데이트합니다
     * @param parentId 부모 블록 ID
     * @param childId 자식 블록 ID
     * @param add true면 추가, false면 제거
     */
    private void updateParentChildrenList(UUID parentId, UUID childId, boolean add) {
        try {
            BlockDocument parentBlock = getBlockDocumentOrThrow(parentId);
            List<UUID> childrenIds = parentBlock.getChildrenIds();
            
            if (childrenIds == null) {
                childrenIds = new ArrayList<>();
                parentBlock.setChildrenIds(childrenIds);
            }
            
            if (add) {
                if (!childrenIds.contains(childId)) {
                    childrenIds.add(childId);
                }
            } else {
                childrenIds.remove(childId);
            }
            
            parentBlock.updateTimestamp();
            blockDocumentRepository.save(parentBlock);
            
            log.debug("부모 블록 자식 목록 업데이트: parentId={}, childId={}, add={}, 총 자식 수={}", 
                    parentId, childId, add, childrenIds.size());
        } catch (Exception e) {
            log.warn("부모 블록 자식 목록 업데이트 실패: parentId={}, childId={}, error={}", 
                    parentId, childId, e.getMessage());
        }
    }

    /**
     * 계층 구조로 정렬된 블록 목록을 생성합니다
     * @param blocks 원본 블록 목록
     * @return 계층 구조로 정렬된 블록 DTO 목록
     */
    private List<BlockDTO> buildHierarchicalBlockList(List<BlockDocument> blocks) {
        // 최상위 블록들 (parentId가 null)부터 시작하여 재귀적으로 구성
        return blocks.stream()
                .filter(block -> block.getParentId() == null)
                .sorted(Comparator.comparing(BlockDocument::getOrderIndex))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * BlockDTO를 BlockDocument로 변환합니다
     * @param dto 변환할 DTO
     * @return 변환된 Document
     */
    private BlockDocument convertToDocument(BlockDTO dto) {
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
     * BlockDocument를 BlockDTO로 변환합니다
     * @param document 변환할 Document
     * @return 변환된 DTO
     */
    private BlockDTO convertToDTO(BlockDocument document) {
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
     * 공통 Block 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws BlockOperationException 래핑된 예외
     */
    private void handleAndThrowBlockException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new BlockOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }
}
