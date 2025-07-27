package com.example.TacoHub.Exception;

/**
 * RabbitMQ 관련 예외 (서버/기술 에러 계열)
 * 메시지 브로커와의 통신, 직렬화, 전송 실패 등
 */
public class RabbitMQException extends TechnicalException {
    public RabbitMQException(String message) {
        super(message);
    }
    public RabbitMQException(String message, Throwable cause) {
        super(message, cause);
    }
}
