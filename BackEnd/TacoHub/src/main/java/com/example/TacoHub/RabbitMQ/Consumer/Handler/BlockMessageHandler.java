package com.example.TacoHub.RabbitMQ.Consumer.Handler;

import com.example.TacoHub.Message.NotionCopyMessage.BlockMessage;
import com.example.TacoHub.Message.BaseMessage;
import com.example.TacoHub.Message.NotionCopyMessage.BlockMessage;
import com.example.TacoHub.Message.BaseMessage;
import com.example.TacoHub.Service.NotionCopyService.BlockService;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.RabbitMQException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockMessageHandler extends BaseMessageHandler<BaseMessage> {

    private final ObjectMapper objectMapper;
    private final BlockService blockService;

    @Override
    protected void doHandleMessage(BaseMessage baseMessage, String messageJson) throws Exception {
        final String methodName = "BlockMessageHandler.doHandleMessage";
        log.debug("[{}] 블록 메시지 처리 시작: {}", methodName, messageJson);

        BlockMessage blockMessage = objectMapper.convertValue(baseMessage, BlockMessage.class);

        // 메시지 타입에 따라 분기 처리
        switch (blockMessage.getBlockOperation()) {
            case CREATE:
                handleCreateBlock(blockMessage);
                break;
            case UPDATE:
                handleUpdateBlock(blockMessage);
                break;
            case DELETE:
                handleRemoveBlock(blockMessage);
                break;
            // TODO : Block Operation Enum 에 정의된 다른 작업 처리 분기 구현
            default:
                log.warn("[{}] 알 수 없는 메시지 타입: {}", methodName, baseMessage.getMessageType());
                break;
        }

        log.debug("[{}] 블록 메시지 처리 완료: {}", methodName, messageJson);
    }

    @Override
    protected void handleAndLogError(Exception e, BaseMessage message, String messageJson, String methodName) {
        log.error("[{}] BlockMessageHandler 메시지 처리 중 에러 발생: {}\n메시지: {}", methodName, e.getMessage(), messageJson, e);
    }

    // 하위 처리 메서드들은 에러 발생 시 log 후 throw
    private void handleCreateBlock(BlockMessage blockMessage) throws Exception {
        final String methodName = "BlockMessageHandler.handleCreateBlock";
        try {
            // TODO: Service 호출 구현 예정
            // blockService.createBlock(blockMessage.getBlockDTO());
        } catch (BusinessException e) {
            log.warn("[{}] Block 생성 비즈니스/시스템 오류 - blockId: {}, 사유: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}] Block 생성 Handler 오류 - blockId: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e);
            throw e;
        }
    }

    private void handleUpdateBlock(BlockMessage blockMessage) throws Exception {
        final String methodName = "BlockMessageHandler.handleUpdateBlock";
        try {
            // TODO: Service 호출 구현 예정
            // blockService.updateBlock(blockMessage.getBlockDTO());
        } catch (BusinessException e) {
            log.warn("[{}] Block 수정 비즈니스/시스템 오류 - blockId: {}, 사유: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}] Block 수정 Handler 오류 - blockId: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e);
            throw e;
        }
    }

    private void handleRemoveBlock(BlockMessage blockMessage) throws Exception {
        final String methodName = "BlockMessageHandler.handleRemoveBlock";
        try {
            // TODO: Service 호출 구현 예정
            // blockService.deleteBlock(blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null);
        } catch (BusinessException e) {
            log.warn("[{}] Block 삭제 비즈니스/시스템 오류 - blockId: {}, 사유: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[{}] Block 삭제 Handler 오류 - blockId: {}", methodName, blockMessage.getBlockDTO() != null ? blockMessage.getBlockDTO().getId() : null, e);
            throw e;
        }
    }
}
