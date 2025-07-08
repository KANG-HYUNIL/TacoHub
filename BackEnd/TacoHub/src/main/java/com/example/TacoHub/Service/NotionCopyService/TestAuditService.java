package com.example.TacoHub.Service.NotionCopyService;

import com.example.TacoHub.Logging.AuditLogging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AOP 로깅 테스트를 위한 예시 서비스
 * 실제 WorkSpaceService에 적용하기 전 테스트 용도
 */
@Service
@Slf4j
public class TestAuditService {
    
    /**
     * 기본 감사 로깅 테스트
     */
    @AuditLogging(action = "테스트_기본_로깅")
    public String testBasicLogging(String message) {
        log.info("테스트 메서드 실행: {}", message);
        return "처리 완료: " + message;
    }
    
    /**
     * 파라미터 포함 감사 로깅 테스트
     */
    @AuditLogging(
        action = "테스트_파라미터_로깅", 
        includeParameters = true,
        includeReturnValue = true
    )
    public String testParameterLogging(String param1, Integer param2) {
        log.info("파라미터 테스트: {} {}", param1, param2);
        return String.format("결과: %s-%d", param1, param2);
    }
    
    /**
     * 예외 발생 테스트
     */
    @AuditLogging(
        action = "테스트_예외_로깅",
        includeErrorDetails = true
    )
    public void testExceptionLogging(boolean throwException) {
        if (throwException) {
            throw new RuntimeException("테스트 예외 발생");
        }
        log.info("정상 처리 완료");
    }
    
    /**
     * 성능 측정 테스트
     */
    @AuditLogging(
        action = "테스트_성능_측정",
        includePerformance = true
    )
    public void testPerformanceLogging() throws InterruptedException {
        // 의도적으로 지연 발생
        Thread.sleep(100);
        log.info("성능 테스트 완료");
    }
    
    /**
     * 민감정보 마스킹 테스트
     */
    @AuditLogging(
        action = "테스트_민감정보_마스킹",
        includeParameters = true
    )
    public String testSensitiveDataMasking(String email, String password, String token, String secretKey) {
        log.info("민감정보 마스킹 테스트 실행");
        return "인증 성공";
    }
    
    /**
     * 복잡한 객체 파라미터 테스트
     */
    @AuditLogging(
        action = "테스트_복잡한_객체",
        includeParameters = true,
        includeReturnValue = true
    )
    public TestResult testComplexObjectParameters(TestRequestData requestData) {
        log.info("복잡한 객체 처리: {}", requestData.getTitle());
        
        if (requestData == null) {
            throw new IllegalArgumentException("요청 데이터가 null입니다");
        }
        
        return new TestResult(true, "처리 완료: " + requestData.getTitle());
    }
    
    /**
     * 대용량 파라미터 테스트
     */
    @AuditLogging(
        action = "테스트_대용량_파라미터",
        includeParameters = true
    )
    public String testLargeParameters(String largeText, int[] numbers) {
        log.info("대용량 파라미터 처리 시작");
        return String.format("처리 완료: 텍스트=%d자, 배열=%d개", 
                           largeText != null ? largeText.length() : 0,
                           numbers != null ? numbers.length : 0);
    }
    
    /**
     * 모든 옵션 활성화 테스트
     */
    @AuditLogging(
        action = "테스트_전체_옵션",
        includeParameters = true,
        includeReturnValue = true,
        includePerformance = true,
        includeErrorDetails = true,
        includeUserInfo = true
    )
    public String testAllOptions(String message, int number) throws InterruptedException {
        log.info("전체 옵션 테스트: {} {}", message, number);
        Thread.sleep(50); // 성능 측정용 지연
        return "전체 옵션 테스트 완료";
    }
    
    // 테스트용 데이터 클래스들
    public static class TestRequestData {
        private String title;
        private String description;
        private String secretKey; // 민감정보 테스트용
        private int priority;

        public TestRequestData(String title, String description, String secretKey, int priority) {
            this.title = title;
            this.description = description;
            this.secretKey = secretKey;
            this.priority = priority;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getSecretKey() { return secretKey; }
        public int getPriority() { return priority; }
    }
    
    public static class TestResult {
        private boolean success;
        private String message;
        
        public TestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
