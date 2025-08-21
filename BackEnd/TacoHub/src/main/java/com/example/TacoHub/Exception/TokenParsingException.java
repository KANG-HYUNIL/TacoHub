package com.example.TacoHub.Exception;

/**
 * JWT 토큰 파싱 오류 발생 시 throw하는 예외
 * TechnicalException 상속
 */
public class TokenParsingException extends TechnicalException {
    public TokenParsingException(String message) {
        super(message);
    }
}
