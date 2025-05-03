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


}
