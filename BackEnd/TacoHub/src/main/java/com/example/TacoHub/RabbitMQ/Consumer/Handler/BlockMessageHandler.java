package com.example.TacoHub.Consumer.Handler;

import com.example.TacoHub.Message.NotionCopyMessage.BlockMessage;
import com.example.TacoHub.Message.BaseMessage;
import com.example.TacoHub.Service.NotionCopyService.BlockService;
import com.example.TacoHub.Exception.NotionCopyException.BlockOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockMessageHandler {
    
    private final ObjectMapper objectMapper;
    private final BlockService blockService; 
    

    /**
     * 메시지 처리 메서드
     * 메시지 타입에 따라 적절한 서비스 메서드를 호출
     * @param baseMessage 처리할 메시지 객체
     * @param messageJson 원본 JSON 문자열 (로그용)
     */
    public void handleMessage(BaseMessage baseMessage, String messageJson)
    {
        try
        {
            log.debug("블록 메시지 처리 시작: {}", messageJson);

            BlockMessage blockMessage = objectMapper.convertValue(baseMessage, BlockMessage.class);

            // 메시지 타입에 따라 분기 처리
            switch (blockMessage.getBlockOperation()) {
                case CREATE:
                    handleCreateBlock(blockMessage);
                    break;

                
                case UPDATE :
                    handleUpdateBlock(blockMessage);
                    break;

                case DELETE :
                    handleRemoveBlock(blockMessage);
                    break;


                // TODO : Block Operation Enum 에 정의된 다른 작업 처리 분기 구현

                




                default:
                    log.warn("알 수 없는 메시지 타입: {}", baseMessage.getMessageType());
                    break;
            }

            log.debug("블록 메시지 처리 완료: {}", messageJson);

        } catch (Exception e)
        {

        }
    }


    /**
     * Block 생성 처리 메서드
     * @param blockMessage 처리할 블록 메시지
     */
    private void handleCreateBlock(BlockMessage blockMessage)
    {
        // try {
        //     log.info("Block 생성 처리 시작 - blockId: {}, workspaceId: {}", 
        //             blockMessage.getBlockData().getId(), blockMessage.getWorkspaceId());
            
        //     // TODO: Service 호출 구현 예정
        //     // blockService.createBlock(blockMessage.getBlockData());
            
        //     log.info("Block 생성 처리 완료 - blockId: {}", blockMessage.getBlockData().getId());
            
        // } catch (BlockOperationException e) {
        //     // Service에서 올라온 비즈니스 예외 - 로깅만, 전파 안함
        //     log.warn("Block 생성 비즈니스 오류 - blockId: {}, 사유: {}", 
        //             blockMessage.getBlockData().getId(), e.getMessage());
            
        // } catch (BusinessException e) {
        //     // Service에서 올라온 시스템 예외 - 로깅만, 전파 안함
        //     log.error("Block 생성 시스템 오류 - blockId: {}, 예외타입: {}, 메시지: {}", 
        //             blockMessage.getBlockData().getId(), e.getClass().getSimpleName(), e.getMessage());
            
        // } catch (Exception e) {
        //     // Handler 자체 오류 - Consumer로 전파
        //     log.error("Block 생성 Handler 오류 - blockId: {}", 
        //             blockMessage.getBlockData().getId(), e);
        //     throw new RuntimeException("Block 생성 Handler 처리 실패", e);
        // }
    }

    /**
     * Block 수정 처리 메서드
     * @param blockMessage 처리할 블록 메시지
     */
    private void handleUpdateBlock(BlockMessage blockMessage)
    {
        // try {
        //     log.info("Block 수정 처리 시작 - blockId: {}, workspaceId: {}", 
        //             blockMessage.getBlockData().getId(), blockMessage.getWorkspaceId());
            
        //     // TODO: Service 호출 구현 예정
        //     // blockService.updateBlock(blockMessage.getBlockData());
            
        //     log.info("Block 수정 처리 완료 - blockId: {}", blockMessage.getBlockData().getId());
            
        // } catch (BlockOperationException e) {
        //     // Service에서 올라온 비즈니스 예외 - 로깅만, 전파 안함
        //     log.warn("Block 수정 비즈니스 오류 - blockId: {}, 사유: {}", 
        //             blockMessage.getBlockData().getId(), e.getMessage());
            
        // } catch (BusinessException e) {
        //     // Service에서 올라온 시스템 예외 - 로깅만, 전파 안함
        //     log.error("Block 수정 시스템 오류 - blockId: {}, 예외타입: {}, 메시지: {}", 
        //             blockMessage.getBlockData().getId(), e.getClass().getSimpleName(), e.getMessage());
            
        // } catch (Exception e) {
        //     // Handler 자체 오류 - Consumer로 전파
        //     log.error("Block 수정 Handler 오류 - blockId: {}", 
        //             blockMessage.getBlockData().getId(), e);
        //     throw new RuntimeException("Block 수정 Handler 처리 실패", e);
        // }       
    }

    /**
     * Block 삭제 처리 메서드
     * @param blockMessage 처리할 블록 메시지
     */
    private void handleRemoveBlock(BlockMessage blockMessage)
    {
        // try {
        //     log.info("Block 삭제 처리 시작 - blockId: {}, workspaceId: {}", 
        //             blockMessage.getBlockData().getId(), blockMessage.getWorkspaceId());
            
        //     // TODO: Service 호출 구현 예정
        //     // blockService.deleteBlock(blockMessage.getBlockData().getId());
            
        //     log.info("Block 삭제 처리 완료 - blockId: {}", blockMessage.getBlockData().getId());
            
        // } catch (BlockOperationException e) {
        //     // Service에서 올라온 비즈니스 예외 - 로깅만, 전파 안함
        //     log.warn("Block 삭제 비즈니스 오류 - blockId: {}, 사유: {}", 
        //             blockMessage.getBlockData().getId(), e.getMessage());
            
        // } catch (BusinessException e) {
        //     // Service에서 올라온 시스템 예외 - 로깅만, 전파 안함
        //     log.error("Block 삭제 시스템 오류 - blockId: {}, 예외타입: {}, 메시지: {}", 
        //             blockMessage.getBlockData().getId(), e.getClass().getSimpleName(), e.getMessage());
            
        // } catch (Exception e) {
        //     // Handler 자체 오류 - Consumer로 전파
        //     log.error("Block 삭제 Handler 오류 - blockId: {}", 
        //             blockMessage.getBlockData().getId(), e);
        //     throw new RuntimeException("Block 삭제 Handler 처리 실패", e);
        // }
    }

}
