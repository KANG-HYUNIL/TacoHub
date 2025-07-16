package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 페이지 조회 실패 비즈니스 예외
 * 존재하지 않는 페이지 ID 조회 시 발생
 */
public class PageNotFoundException extends BusinessException {

    public PageNotFoundException(String message) {
        super(message);
    }

    public PageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
