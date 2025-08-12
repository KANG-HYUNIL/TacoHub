package com.example.TacoHub.Controller.NotionCopyController;

import com.example.TacoHub.Converter.NotionCopyConveter.PageConverter;
import com.example.TacoHub.Converter.NotionCopyConveter.BlockConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import com.example.TacoHub.Document.BlockDocument;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Service.NotionCopyService.BlockService;
import com.example.TacoHub.Service.NotionCopyService.PageService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 페이지 관리 REST API Controller
 * 페이지 CRUD 및 블록 연동 기능 제공
 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final PageService pageService;
    private final BlockService blockService;

    /**
     * 페이지 정보 조회
     * @param pageId 조회할 페이지 ID
     * @return PageDTO 페이지 정보
     */
    @GetMapping("/{pageId}")
    public ResponseEntity<ApiResponse<PageDTO>> getPage(@PathVariable UUID pageId) {
        String methodName = "getPage";
        log.info("[{}] 페이지 조회 요청: pageId={}", methodName, pageId);
        
        try {
            PageEntity pageEntity = pageService.getPageEntityOrThrow(pageId);
            PageDTO pageDTO = PageConverter.toDTO(pageEntity);
            
            log.info("[{}] 페이지 조회 성공: pageId={}, title={}", methodName, pageId, pageDTO.getTitle());
            return ResponseEntity.ok(ApiResponse.success("페이지 조회가 완료되었습니다.", pageDTO));
            
        } catch (Exception e) {
            log.error("[{}] 페이지 조회 실패: pageId={}, error={}", methodName, pageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("페이지 조회에 실패했습니다."));
        }
    }

    /**
     * 페이지의 모든 블록 조회 (계층 구조 포함)
     * @param pageId 조회할 페이지 ID
     * @return List<BlockDTO> 페이지의 모든 블록 리스트
     */
    @GetMapping("/{pageId}/blocks")
    public ResponseEntity<ApiResponse<List<BlockDTO>>> getPageBlocks(@PathVariable UUID pageId) {
        String methodName = "getPageBlocks";
        log.info("[{}] 페이지 블록 조회 요청: pageId={}", methodName, pageId);
        
        try {
            // 1. 페이지 존재 확인
            pageService.getPageEntityOrThrow(pageId);
            
            // 2. 페이지의 모든 블록 조회 (삭제되지 않은 것만, 순서대로)
            List<BlockDocument> blockDocuments = blockService.getBlocksByPageId(pageId);
            
            // 3. BlockDocument -> BlockDTO 변환
            List<BlockDTO> blockDTOs = blockDocuments.stream()
                    .map(BlockConverter::toDTO)
                    .collect(Collectors.toList());
            
            log.info("[{}] 페이지 블록 조회 성공: pageId={}, blockCount={}", methodName, pageId, blockDTOs.size());
            return ResponseEntity.ok(ApiResponse.success("페이지 블록 조회가 완료되었습니다.", blockDTOs));
            
        } catch (Exception e) {
            log.error("[{}] 페이지 블록 조회 실패: pageId={}, error={}", methodName, pageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("페이지 블록 조회에 실패했습니다."));
        }
    }

    /**
     * 페이지 제목 수정
     * @param pageId 수정할 페이지 ID
     * @param title 새로운 제목
     * @return PageDTO 수정된 페이지 정보
     */
    @PutMapping("/{pageId}/title")
    public ResponseEntity<ApiResponse<PageDTO>> updatePageTitle(
            @PathVariable UUID pageId, 
            @RequestBody String title) {
        String methodName = "updatePageTitle";
        log.info("[{}] 페이지 제목 수정 요청: pageId={}, newTitle={}", methodName, pageId, title);
        
        try {
            PageEntity updatedPage = pageService.updatePageTitle(pageId, title);
            PageDTO pageDTO = PageConverter.toDTO(updatedPage);
            
            log.info("[{}] 페이지 제목 수정 성공: pageId={}, newTitle={}", methodName, pageId, title);
            return ResponseEntity.ok(ApiResponse.success("페이지 제목이 수정되었습니다.", pageDTO));
            
        } catch (Exception e) {
            log.error("[{}] 페이지 제목 수정 실패: pageId={}, newTitle={}, error={}", methodName, pageId, title, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("페이지 제목 수정에 실패했습니다."));
        }
    }
 
}
