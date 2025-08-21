package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 워크스페이스 조회 실패 비즈니스 예외
 * 존재하지 않는 워크스페이스 ID 조회 시 발생
 */
public class WorkSpaceNotFoundException extends BusinessException {

    public WorkSpaceNotFoundException(String message) {
        super(message);
    }

    public WorkSpaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
