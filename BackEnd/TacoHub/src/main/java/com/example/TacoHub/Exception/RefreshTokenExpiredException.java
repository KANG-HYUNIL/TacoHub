package com.example.TacoHub.Exception;

/**
 * Refresh Token 만료 시 throw하는 예외
 * TokenExpiredException 상속
 */
public class RefreshTokenExpiredException extends TokenExpiredException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
