package com.example.TacoHub.Exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage());
        return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
    }

    // EmailAlreadyExistsException 처리
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.error("Email already exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<?> handleTechnicalException(TechnicalException e) {
        log.error("Technical error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(InvalidAuthCodeException.class)
    public ResponseEntity<?> handleInvalidAuthCodeException(InvalidAuthCodeException e) {
        log.error("Invalid auth code: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(InvalidLoginRequestException.class)
    public ResponseEntity<?> handleInvalidLoginRequestException(InvalidLoginRequestException e) {
        log.error("Invalid login request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    // WorkSpace 관련 예외 처리
    @ExceptionHandler(com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException.class)
    public ResponseEntity<?> handleWorkSpaceOperationException(com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException e) {
        log.error("워크스페이스 작업 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("워크스페이스 작업 중 오류가 발생했습니다: " + e.getMessage());
    }

    @ExceptionHandler(com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException.class)
    public ResponseEntity<?> handleWorkSpaceNotFoundException(com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException e) {
        log.warn("워크스페이스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("요청한 워크스페이스를 찾을 수 없습니다: " + e.getMessage());
    }

    @ExceptionHandler(com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException.class)
    public ResponseEntity<?> handlePageNotFoundException(com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException e) {
        log.warn("페이지를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("요청한 페이지를 찾을 수 없습니다: " + e.getMessage());
    }


}
