package com.example.TacoHub.Exception.NotionCopyException;

/**
 * 블록을 찾을 수 없을 때 발생하는 예외
 */
public class BlockNotFoundException extends RuntimeException {
    
    public BlockNotFoundException(String message) {
        super(message);
    }
    
    public BlockNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
