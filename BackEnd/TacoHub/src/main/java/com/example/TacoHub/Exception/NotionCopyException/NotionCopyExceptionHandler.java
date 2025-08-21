package com.example.TacoHub.Exception.NotionCopyException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.TacoHub.Dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notion Copy 관련 전역 예외 처리기
 */
@RestControllerAdvice(basePackages = "com.example.TacoHub.Controller.NotionCopyController")
@Slf4j
public class NotionCopyExceptionHandler {


    /**
     * 워크스페이스 관련 예외 처리
     */
    @ExceptionHandler(WorkSpaceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleWorkspaceNotFound(WorkSpaceNotFoundException ex) {
        log.warn("워크스페이스 조회 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("WORKSPACE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(WorkSpaceOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleWorkspaceOperation(WorkSpaceOperationException ex) {
        log.warn("워크스페이스 작업 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("WORKSPACE_OPERATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }

    /**
     * 페이지 관련 예외 처리
     */
    @ExceptionHandler(PageNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handlePageNotFound(PageNotFoundException ex) {
        log.warn("페이지 조회 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("PAGE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(PageOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handlePageOperation(PageOperationException ex) {
        log.warn("페이지 작업 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("PAGE_OPERATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }

    /**
     * 블록 관련 예외 처리
     */
    @ExceptionHandler(BlockNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleBlockNotFound(BlockNotFoundException ex) {
        log.warn("블록 조회 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("BLOCK_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(BlockOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleBlockOperation(BlockOperationException ex) {
        log.warn("블록 작업 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("BLOCK_OPERATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }

    /**
     * 유효성 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("요청 유효성 검증 실패: {}", ex.getMessage());
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        ErrorResponseDTO error = new ErrorResponseDTO("VALIDATION_ERROR", "요청 데이터가 유효하지 않습니다", validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }


    /**
     * WorkSpaceUser 관련 예외 처리
     */
    @ExceptionHandler(WorkSpaceUserNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleWorkSpaceUserNotFound(WorkSpaceUserNotFoundException ex) {
        log.warn("워크스페이스 사용자 조회 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("WORKSPACE_USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error.message, error.error, error));
    }

    @ExceptionHandler(WorkSpaceUserOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleWorkSpaceUserOperation(WorkSpaceUserOperationException ex) {
        log.warn("워크스페이스 사용자 작업 실패: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO("WORKSPACE_USER_OPERATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error.message, error.error, error));
    }


    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponseDTO>> handleGeneral(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        ErrorResponseDTO error = new ErrorResponseDTO("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error.message, error.error, error));
    }
}
