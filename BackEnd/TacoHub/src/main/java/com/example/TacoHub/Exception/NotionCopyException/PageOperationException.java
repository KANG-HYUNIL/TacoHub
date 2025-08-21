package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 페이지 작업 관련 비즈니스 예외
 * 사용자 입력 오류, 권한 부족 등 예상 가능한 비즈니스 로직 위반 시 발생
 */
public class PageOperationException extends BusinessException {
    
    public PageOperationException(String message) {
        super(message);
    }
    
    public PageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
