package com.example.TacoHub.Exception;

/**
 * 기술적/시스템 예외
 * 데이터베이스 연결 오류, 외부 시스템 오류 등 시스템 레벨 문제 시 발생
 */
public class TechnicalException extends SystemException {

    public TechnicalException(String message) {
        super(message);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
