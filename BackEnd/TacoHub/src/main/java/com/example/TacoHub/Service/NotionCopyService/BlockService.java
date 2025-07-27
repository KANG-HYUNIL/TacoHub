package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.TacoHub.Document.BlockDocument;
import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Exception.NotionCopyException.BlockNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.BlockOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Repository.NotionCopyRepository.BlockDocumentRepository;
import com.example.TacoHub.Converter.NotionCopyConveter.BlockConverter;
import com.example.TacoHub.Service.BaseService;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 블록 관련 비즈니스 로직을 처리하는 서비스 클래스
 * Notion과 같은 블록 기반 에디터의 블록 CRUD 및 관리 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService extends BaseService {

    private final BlockDocumentRepository blockDocumentRepository;

    // ========== BlockDocument CRUD 메서드 ========== 

    /**
     * BlockDocument 생성
     * @param blockDTO 생성할 블록 DTO
     * @return 생성된 BlockDocument
     */
    public BlockDocument createBlock(BlockDTO blockDTO) {
        String methodName = "createBlock";
        log.info("[{}] 블록 생성 시작: blockId={}", methodName, blockDTO != null ? blockDTO.getId() : null);
        try {
            // 입력값 검증 (예: blockId, pageId 등)
            validateBlockId(blockDTO.getId(), "blockId");
            validatePageId(blockDTO.getPageId(), "pageId");

            // DTO -> Entity 변환
            BlockDocument entity = BlockConverter.toDocument(blockDTO);
            BlockDocument saved = blockDocumentRepository.save(entity);
            log.info("[{}] 블록 생성 완료: blockId={}", methodName, saved.getId());
            return saved;
        } catch (BlockOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowBlockException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * BlockDocument 단건 조회
     * @param blockId 블록 ID
     * @return 조회된 BlockDocument
     */
    public BlockDocument getBlockById(UUID blockId) {
        String methodName = "getBlockById";
        log.info("[{}] 블록 조회 시작: blockId={}", methodName, blockId);
        try {
            validateBlockId(blockId, "blockId");
            BlockDocument doc = getBlockDocumentOrThrow(blockId);
            log.info("[{}] 블록 조회 완료: blockId={}", methodName, blockId);
            return doc;
        } catch (BlockOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowBlockException(methodName, e);
            return null;
        }
    }

    /**
     * BlockDocument 수정
     * @param blockDTO 수정할 블록 DTO
     * @return 수정된 BlockDocument
     */
    public BlockDocument updateBlock(BlockDTO blockDTO) {
        String methodName = "updateBlock";
        log.info("[{}] 블록 수정 시작: blockId={}", methodName, blockDTO != null ? blockDTO.getId() : null);
        try {
            validateBlockId(blockDTO.getId(), "blockId");
            validatePageId(blockDTO.getPageId(), "pageId");
            BlockDocument existing = getBlockDocumentOrThrow(blockDTO.getId());
            // DTO -> Entity 변환 및 필드 업데이트
            // 기존 엔티티에 DTO 값 복사 (id, pageId 등은 불변, 나머지 필드만 업데이트)
            existing.setBlockType(blockDTO.getBlockType());
            existing.setContent(blockDTO.getContent());
            existing.setProperties(blockDTO.getProperties());
            existing.setParentId(blockDTO.getParentId());
            existing.setOrderIndex(blockDTO.getOrderIndex());
            existing.setChildrenIds(blockDTO.getChildrenIds());
            existing.setHasChildren(blockDTO.getHasChildren());
            existing.setMetadata(blockDTO.getMetadata());
            existing.setUpdatedAt(blockDTO.getUpdatedAt());
            existing.setLastEditedBy(blockDTO.getLastEditedBy());
            BlockDocument saved = blockDocumentRepository.save(existing);
            log.info("[{}] 블록 수정 완료: blockId={}", methodName, saved.getId());
            return saved;
        } catch (BlockOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowBlockException(methodName, e);
            return null;
        }
    }

    /**
     * BlockDocument 삭제 (soft delete)
     * @param blockId 삭제할 블록 ID
     */
    public void deleteBlock(UUID blockId) {
        String methodName = "deleteBlock";
        log.info("[{}] 블록 삭제 시작: blockId={}", methodName, blockId);
        try {
            validateBlockId(blockId, "blockId");
            BlockDocument doc = getBlockDocumentOrThrow(blockId);
            doc.setIsDeleted(true);
            blockDocumentRepository.save(doc);
            log.info("[{}] 블록 삭제 완료(soft): blockId={}", methodName, blockId);
        } catch (BlockOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowBlockException(methodName, e);
        }
    }

    // ========== 공통 검증 메서드 ==========
    
    /**
     * 블록 ID 유효성 검증
     * @param blockId 검증할 블록 ID
     * @param paramName 매개변수명 (로그용)
     */
    private void validateBlockId(UUID blockId, String paramName) {
        if (isNull(blockId)) {
            throw new BlockOperationException(paramName + "은(는) 필수 입력 항목입니다. 블록 ID를 지정해주세요.");
        }
    }

    /**
     * 페이지 ID 유효성 검증
     * @param pageId 검증할 페이지 ID
     * @param paramName 매개변수명 (로그용)
     */
    private void validatePageId(UUID pageId, String paramName) {
        if (isNull(pageId)) {
            throw new BlockOperationException(paramName + "은(는) 필수 입력 항목입니다. 페이지 ID를 지정해주세요.");
        }
    }


    /**
     * 페이지 ID로 해당 페이지의 모든 블록을 삭제합니다
     * @param pageId 삭제할 블록들이 속한 페이지 ID
     */
    @Transactional
    public void deleteBlockByPageId(UUID pageId) {
        String methodName = "deleteBlockByPageId";
        log.info("[{}] 페이지 블록 삭제 시작: pageId={}", methodName, pageId);
        
        try {
            // 1. 입력값 검증
            validatePageId(pageId, "페이지 ID");

            // 2. 페이지에 속한 모든 블록 삭제
            blockDocumentRepository.deleteByPageId(pageId);
            
            log.info("[{}] 페이지 블록 삭제 완료: pageId={}", methodName, pageId);

        } catch (BlockOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowBlockException(methodName, e);
        }
    }


    /**
     * 블록 ID로 BlockDocument를 조회하고, 존재하지 않으면 예외를 발생시킵니다
     * @param blockId 조회할 블록 ID
     * @return 조회된 BlockDocument
     * @throws BlockNotFoundException 블록이 존재하지 않거나 삭제된 경우
     */
    private BlockDocument getBlockDocumentOrThrow(UUID blockId) {
        if (blockId == null) {
            throw new BlockOperationException("블록 ID는 필수입니다");
        }
        
        return blockDocumentRepository.findByIdAndIsDeleted(blockId, false)
                .orElseThrow(() -> new BlockNotFoundException("블록을 찾을 수 없습니다: " + blockId));
    }


    /**
     * 공통 Block 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws BlockOperationException 래핑된 예외
     */
    private void handleAndThrowBlockException(String methodName, Exception originalException) {
        BlockOperationException customException = new BlockOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }
}
