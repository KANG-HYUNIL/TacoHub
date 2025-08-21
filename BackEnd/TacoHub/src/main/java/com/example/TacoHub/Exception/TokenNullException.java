package com.example.TacoHub.Exception;

/**
 * JWT 토큰이 null일 때 throw하는 예외
 * BusinessException 상속
 */
public class TokenNullException extends BusinessException {
    public TokenNullException(String message) {
        super(message);
    }
}
