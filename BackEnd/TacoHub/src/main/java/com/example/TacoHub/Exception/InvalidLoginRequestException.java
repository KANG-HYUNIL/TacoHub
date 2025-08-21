package com.example.TacoHub.Exception;

/**
 * 잘못된 로그인 요청 비즈니스 예외
 * 로그인 시 필수 필드 누락 또는 형식 오류 시 발생
 */
public class InvalidLoginRequestException extends BusinessException {
    
    public InvalidLoginRequestException(String message) {
        super(message);
    }

    public InvalidLoginRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
