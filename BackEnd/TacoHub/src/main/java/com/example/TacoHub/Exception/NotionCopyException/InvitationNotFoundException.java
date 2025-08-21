package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 초대를 찾을 수 없을 때 발생하는 예외 클래스
 */
public class InvitationNotFoundException extends BusinessException {
    
    public InvitationNotFoundException(String message) {
        super(message);
    }
    
    public InvitationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
