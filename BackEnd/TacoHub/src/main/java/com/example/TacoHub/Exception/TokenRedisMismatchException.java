package com.example.TacoHub.Exception;

/**
 * Redis에 저장된 토큰과 불일치 시 throw하는 예외
 * BusinessException 상속
 */
public class TokenRedisMismatchException extends BusinessException {
    public TokenRedisMismatchException(String message) {
        super(message);
    }
}
