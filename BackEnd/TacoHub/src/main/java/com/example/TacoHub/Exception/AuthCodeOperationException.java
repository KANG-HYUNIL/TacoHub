package com.example.TacoHub.Exception;

/**
 * 인증 코드 작업 비즈니스 예외
 * 잘못된 인증 코드, 만료된 코드 등 인증 코드 관련 비즈니스 규칙 위반 시 발생
 */
public class AuthCodeOperationException extends BusinessException {
    
    public AuthCodeOperationException(String message) {
        super(message);
    }

    public AuthCodeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
