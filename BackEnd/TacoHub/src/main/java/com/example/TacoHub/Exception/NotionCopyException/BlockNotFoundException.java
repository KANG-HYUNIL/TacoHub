package com.example.TacoHub.Exception.NotionCopyException;

import com.example.TacoHub.Exception.BusinessException;

/**
 * 블록 조회 실패 비즈니스 예외
 * 존재하지 않는 블록 ID 조회 시 발생
 */
public class BlockNotFoundException extends BusinessException {
    
    public BlockNotFoundException(String message) {
        super(message);
    }
    
    public BlockNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
