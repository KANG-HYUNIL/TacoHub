package com.example.TacoHub.Exception;

/**
 * 인증 코드 불일치 비즈니스 예외
 * 잘못된 인증 코드 입력 시 발생
 */
public class InvalidAuthCodeException extends BusinessException {

    public InvalidAuthCodeException(String message) {
        super(message);
    }

    public InvalidAuthCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
