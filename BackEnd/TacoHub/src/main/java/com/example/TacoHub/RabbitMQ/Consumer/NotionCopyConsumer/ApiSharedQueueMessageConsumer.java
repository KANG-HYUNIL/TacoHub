package com.example.TacoHub.RabbitMQ.Consumer.NotionCopyConsumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.TacoHub.Message.BaseMessage;
import com.example.TacoHub.RabbitMQ.Consumer.Handler.BlockMessageHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiSharedQueueMessageConsumer {

    private final ObjectMapper objectMapper;
    private final BlockMessageHandler blockMessageHandler;

    @Value("${rabbitmq.queues.api-server-shared}")
    private String API_SERVER_SHRAED_QUEUE;

    /**
     * API 서버 전용 Queue에서 메시지 소비
     */
    @RabbitListener(queues = "${rabbitmq.queues.api-server-shared}")
    public void consumeApiMessage(String messageJson)
    {
        try {

            log.debug("API 서버 전용 메시지 수신: {}", messageJson);

            // JSON을 BaseMessage로 파싱
            BaseMessage baseMessage = objectMapper.readValue(messageJson, BaseMessage.class);

            // 메시지 타입에 따라 분기 처리
            routeMessage(baseMessage, messageJson);

        } catch (JsonProcessingException e)
        {
            log.error("메시지 파싱 오류: {}", messageJson, e);


        } catch (Exception e)
        {
            log.error("API 서버 전용 메시지 처리 중 오류 발생: {}", messageJson, e);
            // 필요시 DLQ(Dead Letter Queue)로 전송
        }


    }



    /**
     * 메시지 타입에 따른 라우팅
     */
    private void routeMessage(BaseMessage baseMessage, String messageJson) 
    {
        // 메시지 타입에 따라 분기 처리
        switch (baseMessage.getMessageType()) {
            case BLOCK:
                // TYPE_A 메시지 처리 로직
                // TODO : 
                blockMessageHandler.handleMessage(baseMessage, messageJson);

                break;
            case PAGE:
                // TYPE_B 메시지 처리 로직
                // TODO : 

                break;
            case WORKSPACE:
                // TYPE_C 메시지 처리 로직
                // TODO : 

                break;
            default:
                log.warn("알 수 없는 메시지 타입: {}", baseMessage.getMessageType());
        }
    }

}
