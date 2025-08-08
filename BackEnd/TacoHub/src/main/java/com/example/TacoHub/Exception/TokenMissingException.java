package com.example.TacoHub.Exception;

/**
 * JWT 토큰이 누락된 경우 throw하는 예외
 * BusinessException 상속
 */
public class TokenMissingException extends BusinessException {
    public TokenMissingException(String message) {
        super(message);
    }
}
