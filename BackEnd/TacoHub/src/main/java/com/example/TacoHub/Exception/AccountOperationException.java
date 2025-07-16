package com.example.TacoHub.Exception;

/**
 * 계정 작업 관련 비즈니스 예외
 * 사용자 입력 오류, 권한 부족 등 예상 가능한 비즈니스 로직 위반 시 발생
 */
public class AccountOperationException extends BusinessException {
    
    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
