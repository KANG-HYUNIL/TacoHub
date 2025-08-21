package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 초대 작업 관련 비즈니스 예외 클래스
 */
public class InvitationOperationException extends BusinessException {
    
    public InvitationOperationException(String message) {
        super(message);
    }
    
    public InvitationOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
