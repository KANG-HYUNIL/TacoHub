package com.example.TacoHub.Exception;

/**
 * 이메일 작업 시스템 예외
 * SMTP 서버 오류, 네트워크 연결 문제 등 이메일 전송 관련 시스템 문제 시 발생
 */
public class EmailOperationException extends SystemException {
    
    public EmailOperationException(String message) {
        super(message);
    }

    public EmailOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
