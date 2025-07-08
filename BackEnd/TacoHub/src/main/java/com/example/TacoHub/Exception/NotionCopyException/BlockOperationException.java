package com.example.TacoHub.Exception.NotionCopyException;

/**
 * 블록 관련 작업 중 발생하는 예외
 */
public class BlockOperationException extends RuntimeException {
    
    public BlockOperationException(String message) {
        super(message);
    }
    
    public BlockOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
