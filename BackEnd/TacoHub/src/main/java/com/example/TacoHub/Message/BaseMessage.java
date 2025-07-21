package com.example.TacoHub.Message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.example.TacoHub.Enum.NotionCopyEnum.MessageEnum.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ 메시지의 기본 구조
 * 모든 메시지 타입의 공통 필드를 정의
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseMessage {
    
    @JsonProperty("messageId")
    private String messageId = UUID.randomUUID().toString();
    
    @JsonProperty("messageType")
    private MessageType messageType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 라우팅 키 생성 메서드 (각 메시지 타입에서 구현)
     * @return routing key (예: "block.update.workspace-123")
     */
    public abstract String getRoutingKey();
    
    /**
     * 메시지 타입 반환 (라우팅 및 로깅용)
     * @return 메시지 타입 Enum
     */
    public MessageType getMessageType() {
        return this.messageType;
    }
    
    /**
     * 메시지 타입 설정 (생성자에서 호출)
     * @param messageType 메시지 타입
     */
    protected void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
