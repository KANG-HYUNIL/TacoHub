package com.example.TacoHub.Exception;

/**
 * 비즈니스 예외의 기본 클래스
 * 이 예외를 상속받은 모든 예외는 WARN 레벨로 로깅됩니다.
 */
public abstract class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
