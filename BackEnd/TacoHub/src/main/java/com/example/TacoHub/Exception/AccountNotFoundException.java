package com.example.TacoHub.Exception;

/**
 * 사용자 계정 조회 실패 비즈니스 예외
 * 존재하지 않는 사용자 계정 조회 시 발생
 */
public class AccountNotFoundException extends BusinessException {
    
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
