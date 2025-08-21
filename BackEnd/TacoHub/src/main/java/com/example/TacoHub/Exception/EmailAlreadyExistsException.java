package com.example.TacoHub.Exception;

/**
 * 이메일 중복 비즈니스 예외
 * 회원가입 시 이미 존재하는 이메일 사용 시 발생
 */
public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
