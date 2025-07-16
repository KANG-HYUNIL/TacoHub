package com.example.TacoHub.Exception;

/**
 * Redis 작업 시스템 예외
 * Redis 연결 오류, 직렬화/역직렬화 오류 등 Redis 관련 시스템 문제 시 발생
 */
public class RedisOperationException extends SystemException {
    
    public RedisOperationException(String message) {
        super(message);
    }

    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
