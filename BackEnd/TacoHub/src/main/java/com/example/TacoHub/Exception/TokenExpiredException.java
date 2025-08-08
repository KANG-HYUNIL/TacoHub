package com.example.TacoHub.Exception;

/**
 * JWT 토큰 만료 시 throw하는 예외
 * BusinessException을 상속
 */
public class TokenExpiredException extends BusinessException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
