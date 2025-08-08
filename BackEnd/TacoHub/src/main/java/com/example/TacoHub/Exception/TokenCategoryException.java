
package com.example.TacoHub.Exception;

/**
 * JWT 토큰 category 오류 발생 시 throw하는 예외
 * BusinessException 상속
 */
public class TokenCategoryException extends BusinessException {
    public TokenCategoryException(String message) {
        super(message);
    }
}
