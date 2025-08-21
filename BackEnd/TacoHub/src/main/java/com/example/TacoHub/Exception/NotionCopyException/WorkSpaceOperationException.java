package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 워크스페이스 작업 관련 비즈니스 예외
 * 사용자 입력 오류, 권한 부족 등 예상 가능한 비즈니스 로직 위반 시 발생
 */
public class WorkSpaceOperationException extends BusinessException {

    public WorkSpaceOperationException(String message) {
        super(message);
    }

    public WorkSpaceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
