package com.example.TacoHub.Logging;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 감사 로그 정보를 담는 데이터 모델
 * 로깅된 정보를 구조화하여 저장/전송하는 용도
 */
@Data
@Builder
public class AuditLog {
    
    // 기본 추적 정보
    private String traceId;           // 요청 추적 ID
    private String sessionId;        // 세션 ID
    private LocalDateTime timestamp;  // 로그 발생 시간
    
    // 메서드 실행 정보
    private String className;         // 클래스명
    private String methodName;        // 메서드명
    private String action;           // 액션명
    private Long executionTimeMs;    // 실행 시간(밀리초)
    private String status;           // 실행 상태 (SUCCESS/ERROR)
    
    // 사용자 정보 (SecurityContext에서 추출)
    private String userId;           // 사용자 ID
    private String userEmail;        // 사용자 이메일
    private String userRole;         // 사용자 권한
    private String clientIp;         // 클라이언트 IP
    private String userAgent;       // User-Agent
    
    // 메서드 입출력 정보
    private Map<String, Object> parameters;  // 입력 파라미터
    private Object returnValue;              // 반환값
    
    // 오류 정보
    private String errorType;        // 예외 타입
    private String errorMessage;     // 오류 메시지
    private String stackTrace;       // 스택 트레이스 (선택적)
    
    // 비즈니스 컨텍스트 (Notion Copy Service 관련)
    private String workspaceId;     // 워크스페이스 ID
    private String pageId;          // 페이지 ID
    private String blockId;         // 블록 ID
    
}
