
 

 
package com.example.TacoHub.Exception;

import com.example.TacoHub.Dto.ErrorResponseDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleGlobalException(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INTERNAL_SERVER_ERROR", "예상치 못한 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error.message, error.error, error));
    }

    // EmailAlreadyExistsException 처리
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.error("Email already exists: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("EMAIL_ALREADY_EXISTS", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTechnicalException(TechnicalException e) {
        log.error("Technical error: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TECHNICAL_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(InvalidAuthCodeException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleInvalidAuthCodeException(InvalidAuthCodeException e) {
        log.error("Invalid auth code: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INVALID_AUTH_CODE", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(InvalidLoginRequestException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleInvalidLoginRequestException(InvalidLoginRequestException e) {
        log.error("Invalid login request: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INVALID_LOGIN_REQUEST", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error.message, error.error, error));
    }

    // Account 관련 예외 처리
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleAccountNotFoundException(AccountNotFoundException e) {
        log.warn("Account not found: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("ACCOUNT_NOT_FOUND", "요청한 계정을 찾을 수 없습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(AccountOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleAccountOperationException(AccountOperationException e) {
        log.error("Account operation failed: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("ACCOUNT_OPERATION_ERROR", "계정 작업 중 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error.message, error.error, error));
    }


        // JWT 토큰 만료 예외 처리
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTokenExpiredException(TokenExpiredException e) {
        log.warn("Token expired: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TOKEN_EXPIRED", "토큰이 만료되었습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.error(error.message, error.error, error));
    }

       // Refresh Token 만료 예외 처리
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleRefreshTokenExpiredException(RefreshTokenExpiredException e) {
        log.warn("Refresh token expired: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("REFRESH_TOKEN_EXPIRED", "Refresh 토큰이 만료되었습니다: " + e.getMessage());
        return ResponseEntity.status(419).body(ApiResponse.error(error.message, error.error, error));
    }

    // JWT 토큰 파싱 오류
    @ExceptionHandler(TokenParseException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTokenParseException(TokenParseException e) {
        log.error("Token parse error: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TOKEN_PARSE_ERROR", "토큰 파싱 오류: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(error.message, error.error, error)); // 422
    }

    // JWT 토큰 null 예외
    @ExceptionHandler(TokenNullException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTokenNullException(TokenNullException e) {
        log.warn("Token null: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TOKEN_NULL", "토큰이 존재하지 않습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error)); // 400
    }

    // JWT 토큰 category 불일치 예외
    @ExceptionHandler(TokenCategoryException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTokenCategoryException(TokenCategoryException e) {
        log.warn("Token category error: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TOKEN_CATEGORY_ERROR", "토큰 유형이 올바르지 않습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error.message, error.error, error)); // 401
    }

    // Redis 토큰 불일치 예외
    @ExceptionHandler(TokenRedisMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleTokenRedisMismatchException(TokenRedisMismatchException e) {
        log.warn("Token redis mismatch: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TOKEN_REDIS_MISMATCH", "토큰 정보가 서버와 일치하지 않습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error.message, error.error, error)); // 401
    }


}
