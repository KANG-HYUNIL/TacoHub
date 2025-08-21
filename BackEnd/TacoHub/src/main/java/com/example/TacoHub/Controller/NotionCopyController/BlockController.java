package com.example.TacoHub.Controller.NotionCopyController;

import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import com.example.TacoHub.Document.BlockDocument;
import com.example.TacoHub.Converter.NotionCopyConveter.BlockConverter;
import com.example.TacoHub.Service.NotionCopyService.BlockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * 블록 관리 REST API Controller
 * 블록 CRUD 및 계층 구조 관리 기능 제공
 */
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Slf4j
public class BlockController {

    private final BlockService blockService;

    /**
     * 블록 생성
     * @param blockDTO 생성할 블록 정보
     * @return BlockDTO 생성된 블록 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BlockDTO>> createBlock(@Valid @RequestBody BlockDTO blockDTO) {
        String methodName = "createBlock";
        log.info("[{}] 블록 생성 요청: pageId={}, blockType={}", methodName, blockDTO.getPageId(), blockDTO.getBlockType());
        
        try {
            BlockDocument createdBlock = blockService.createBlock(blockDTO);
            BlockDTO responseDTO = BlockConverter.toDTO(createdBlock);
            
            log.info("[{}] 블록 생성 성공: blockId={}", methodName, createdBlock.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("블록이 생성되었습니다.", responseDTO));
            
        } catch (Exception e) {
            log.error("[{}] 블록 생성 실패: pageId={}, error={}", methodName, blockDTO.getPageId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("블록 생성에 실패했습니다."));
        }
    }

    /**
     * 블록 조회
     * @param blockId 조회할 블록 ID
     * @return BlockDTO 블록 정보
     */
    @GetMapping("/{blockId}")
    public ResponseEntity<ApiResponse<BlockDTO>> getBlock(@PathVariable UUID blockId) {
        String methodName = "getBlock";
        log.info("[{}] 블록 조회 요청: blockId={}", methodName, blockId);
        
        try {
            BlockDocument blockDocument = blockService.getBlockById(blockId);
            BlockDTO blockDTO = BlockConverter.toDTO(blockDocument);
            
            log.info("[{}] 블록 조회 성공: blockId={}", methodName, blockId);
            return ResponseEntity.ok(ApiResponse.success("블록 조회가 완료되었습니다.", blockDTO));
            
        } catch (Exception e) {
            log.error("[{}] 블록 조회 실패: blockId={}, error={}", methodName, blockId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("블록 조회에 실패했습니다."));
        }
    }

    /**
     * 블록 수정
     * @param blockId 수정할 블록 ID
     * @param blockDTO 수정할 블록 정보
     * @return BlockDTO 수정된 블록 정보
     */
    @PutMapping("/{blockId}")
    public ResponseEntity<ApiResponse<BlockDTO>> updateBlock(
            @PathVariable UUID blockId, 
            @Valid @RequestBody BlockDTO blockDTO) {
        String methodName = "updateBlock";
        log.info("[{}] 블록 수정 요청: blockId={}", methodName, blockId);
        
        try {
            // Path Variable의 blockId와 RequestBody의 id 일치 확인
            if (!blockId.equals(blockDTO.getId())) {
                log.warn("[{}] 블록 ID 불일치: pathId={}, bodyId={}", methodName, blockId, blockDTO.getId());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("블록 ID가 일치하지 않습니다."));
            }
            
            BlockDocument updatedBlock = blockService.updateBlock(blockDTO);
            BlockDTO responseDTO = BlockConverter.toDTO(updatedBlock);
            
            log.info("[{}] 블록 수정 성공: blockId={}", methodName, blockId);
            return ResponseEntity.ok(ApiResponse.success("블록이 수정되었습니다.", responseDTO));
            
        } catch (Exception e) {
            log.error("[{}] 블록 수정 실패: blockId={}, error={}", methodName, blockId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("블록 수정에 실패했습니다."));
        }
    }

    /**
     * 블록 삭제
     * @param blockId 삭제할 블록 ID
     * @return ResponseEntity 삭제 결과
     */
    @DeleteMapping("/{blockId}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(@PathVariable UUID blockId) {
        String methodName = "deleteBlock";
        log.info("[{}] 블록 삭제 요청: blockId={}", methodName, blockId);
        
        try {
            blockService.deleteBlock(blockId);
            
            log.info("[{}] 블록 삭제 성공: blockId={}", methodName, blockId);
            return ResponseEntity.ok(ApiResponse.success("블록이 삭제되었습니다.", null));
            
        } catch (Exception e) {
            log.error("[{}] 블록 삭제 실패: blockId={}, error={}", methodName, blockId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("블록 삭제에 실패했습니다."));
        }
    }

}
