package com.example.TacoHub.RabbitMQ.Producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.TacoHub.Message.BaseMessage;

/**
 * @fileoverview 실시간 협업(Collaboration) 메시지 RabbitMQ Producer
 *
 * 이 클래스는 API 서버에서 발생한 실시간 협업 관련 이벤트(예: 블록 편집, 페이지 변경 등)를
 * RabbitMQ Exchange/Queue로 publish(전송)하는 역할을 담당합니다.
 *
 * 주요 기능:
 * - 메시지 생성 및 직렬화
 * - 지정된 Exchange/Queue로 메시지 publish
 * - 메시지 전송 성공/실패 로깅 및 예외 처리
 *
 * @author TacoHub Team
 * @version 1.0.0
 */
@Component
public class CollaborationMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    // TODO : RabbitMQ Exchange, RoutingKey 설정값을 application.yml에서 주입받도록 수정
    

    public CollaborationMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 협업 메시지를 RabbitMQ Exchange로 publish하는 메서드
     * @param message 전송할 메시지 객체(직렬화 가능)
     */
    public void sendCollaborationMessage(BaseMessage message) {
        // 1. 메시지 유효성 검사 및 직렬화
        // 2. Exchange, RoutingKey 지정
        // 3. RabbitTemplate을 이용해 메시지 publish
        // 4. 성공/실패 로깅 및 예외 처리
    }

    /**
     * (선택) 특정 타입의 메시지 전송 메서드 예시
     * @param blockUpdateMessage BlockUpdateMessage DTO
     */
    // public void sendBlockUpdateMessage(BlockUpdateMessage blockUpdateMessage) {
    //     // 1. blockUpdateMessage 유효성 검사
    //     // 2. sendCollaborationMessage(blockUpdateMessage) 호출
    // }
}