package com.example.TacoHub.Exception.NotionCopyException;

/**
 * 페이지 작업 중 발생하는 비즈니스 예외
 * 예상치 못한 시스템 예외를 래핑하는 용도로 사용
 */
public class PageOperationException extends RuntimeException {
    
    public PageOperationException(String message) {
        super(message);
    }
    
    public PageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
