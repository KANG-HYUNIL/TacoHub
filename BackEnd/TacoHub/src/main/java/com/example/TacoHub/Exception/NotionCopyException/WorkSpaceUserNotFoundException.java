package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 워크스페이스 사용자 조회 실패 비즈니스 예외
 * 존재하지 않는 워크스페이스 사용자 조회 시 발생
 */
public class WorkSpaceUserNotFoundException extends BusinessException {

    public WorkSpaceUserNotFoundException(String message) {
        super(message);
    }

    public WorkSpaceUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
