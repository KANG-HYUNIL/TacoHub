package com.example.TacoHub.Exception;

public class RedisOperationException extends RuntimeException {
    
    public RedisOperationException(String message) {
        super(message);
    }

    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RedisOperationException(Throwable cause) {
        super(cause);
    }
    
}
