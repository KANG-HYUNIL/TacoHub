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
import com.example.TacoHub.Converter.NotionCopyConveter.BlockConverter;
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


    private final BlockDocumentRepository blockDocumentRepository;

    @Transactional
    public void deleteBlockByPageId(UUID pageId)
    {
        try {
            if (pageId == null) {
                log.warn("페이지 ID가 null");
                throw new BlockOperationException("페이지 ID는 필수입니다");
            }

            // 페이지에 속한 모든 블록 삭제
            blockDocumentRepository.deleteByPageId(pageId);
            log.info("deleteBlockByPageId Success : pageId={}", pageId);
        } catch (Exception e) {
            handleAndThrowBlockException("deleteBlockByPageId", e);
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
