package com.example.TacoHub.Exception;

/**
 * 시스템 예외의 기본 클래스
 * 이 예외를 상속받은 모든 예외는 ERROR 레벨로 로깅됩니다.
 */
public abstract class SystemException extends RuntimeException {
    
    public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
