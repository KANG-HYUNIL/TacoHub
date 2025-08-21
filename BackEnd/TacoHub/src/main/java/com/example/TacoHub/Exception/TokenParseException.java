package com.example.TacoHub.Exception;

/**
 * JWT 토큰 파싱 오류 시 throw하는 예외
 * TechnicalException 상속
 */
public class TokenParseException extends TechnicalException {
    public TokenParseException(String message) {
        super(message);
    }
}
