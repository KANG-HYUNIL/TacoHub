package com.example.TacoHub.Exception;

import com.example.TacoHub.Dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INTERNAL_SERVER_ERROR", "예상치 못한 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // EmailAlreadyExistsException 처리
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.error("Email already exists: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("EMAIL_ALREADY_EXISTS", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponseDTO> handleTechnicalException(TechnicalException e) {
        log.error("Technical error: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("TECHNICAL_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidAuthCodeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidAuthCodeException(InvalidAuthCodeException e) {
        log.error("Invalid auth code: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INVALID_AUTH_CODE", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidLoginRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidLoginRequestException(InvalidLoginRequestException e) {
        log.error("Invalid login request: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("INVALID_LOGIN_REQUEST", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Account 관련 예외 처리
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountNotFoundException(AccountNotFoundException e) {
        log.warn("Account not found: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("ACCOUNT_NOT_FOUND", "요청한 계정을 찾을 수 없습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccountOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountOperationException(AccountOperationException e) {
        log.error("Account operation failed: {}", e.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("ACCOUNT_OPERATION_ERROR", "계정 작업 중 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

   


}
