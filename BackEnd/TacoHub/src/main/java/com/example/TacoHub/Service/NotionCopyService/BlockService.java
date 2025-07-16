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
     * BlockDTO 유효성 검증
     * @param blockDto 검증할 블록 DTO
     * @param paramName 매개변수명 (로그용)
     */
    private void validateBlockDto(BlockDTO blockDto, String paramName) {
        
        // TODO : Block 종류 설립
        // if (isNull(blockDto)) {
        //     throw new BlockOperationException(paramName + "은(는) 필수 입력 항목입니다. 블록 정보를 입력해주세요.");
        // }
        // validatePageId(blockDto.getPageId(), "페이지 ID");
        
        // if (isStringNullOrEmpty(blockDto.getBlockType())) {
        //     throw new BlockOperationException("블록 타입은 필수 입력 항목입니다. 블록 타입을 지정해주세요.");
        // }
        
        // // 허용된 블록 타입인지 검증
        // String[] allowedTypes = {"paragraph", "heading_1", "heading_2", "heading_3", 
        //                         "bulleted_list", "numbered_list", "image", "code", "quote"};
        // boolean isValidType = false;
        // for (String type : allowedTypes) {
        //     if (type.equals(blockDto.getBlockType())) {
        //         isValidType = true;
        //         break;
        //     }
        // }
        // if (!isValidType) {
        //     throw new BlockOperationException("블록 타입이 유효하지 않습니다. 허용된 타입: " + String.join(", ", allowedTypes));
        // }
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
