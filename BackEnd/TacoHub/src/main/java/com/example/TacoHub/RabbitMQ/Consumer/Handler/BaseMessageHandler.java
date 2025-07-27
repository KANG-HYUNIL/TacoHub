package com.example.TacoHub.RabbitMQ.Consumer.Handler;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 메시지 핸들러 공통 추상 클래스
 * 모든 메시지 핸들러는 이 클래스를 상속받아 handleMessage, handleAndLogError를 구현해야 함
 */
@Slf4j
public abstract class BaseMessageHandler<T> {

    /**
     * 메시지 처리 메서드 (최상위 에러 핸들링 포함)
     * @param message 처리할 메시지 객체
     * @param messageJson 원본 JSON 문자열 (로그용)
     */
    public void handleMessage(T message, String messageJson) {
        String methodName = getHandlerMethodName();
        try {
            doHandleMessage(message, messageJson);
        } catch (Exception e) {
            handleAndLogError(e, message, messageJson, methodName);
        }
    }

    /**
     * 실제 메시지 처리 로직 (하위 클래스에서 구현)
     */
    protected abstract void doHandleMessage(T message, String messageJson) throws Exception;

    /**
     * 에러 핸들링 메서드 (하위 클래스에서 구현 또는 상속)
     * @param methodName 에러 발생 메서드명
     */
    protected void handleAndLogError(Exception e, T message, String messageJson, String methodName) {
        log.error("[{}] RabbitMQ 메시지 처리 중 에러 발생: {}\n메시지: {}", methodName, e.getMessage(), messageJson, e);
        // 필요시 추가 알림/모니터링 연동
    }

    /**
     * 하위 클래스에서 메서드명 제공 (로깅용)
     */
    protected String getHandlerMethodName() {
        return this.getClass().getSimpleName() + ".handleMessage";
    }
}
