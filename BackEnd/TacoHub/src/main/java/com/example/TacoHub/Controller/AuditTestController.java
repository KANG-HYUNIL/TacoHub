package com.example.TacoHub.Controller;

import com.example.TacoHub.Service.NotionCopyService.TestAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AOP 로깅 테스트용 컨트롤러
 * 실제 HTTP 요청으로 AOP 로깅 동작을 확인
 */
@RestController
@RequestMapping("/api/audit-test")
@RequiredArgsConstructor
public class AuditTestController {

    private final TestAuditService testAuditService;

    /**
     * 기본 AOP 로깅 테스트
     */
    @GetMapping("/basic")
    public String testBasicLogging(@RequestParam String message) {
        return testAuditService.testBasicLogging(message);
    }

    /**
     * 민감정보 마스킹 테스트
     */
    @PostMapping("/sensitive")
    public String testSensitiveData(@RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String token) {
        return testAuditService.testSensitiveDataMasking(email, password, token, "secret-key-123");
    }

    /**
     * 예외 발생 테스트
     */
    @GetMapping("/exception")
    public String testException(@RequestParam boolean shouldFail) {
        testAuditService.testExceptionLogging(shouldFail);
        return "성공";
    }

    /**
     * 복잡한 객체 테스트
     */
    @PostMapping("/complex")
    public TestAuditService.TestResult testComplexObject(@RequestBody TestRequestData data) {
        // 컨트롤러 DTO를 서비스 DTO로 변환
        TestAuditService.TestRequestData serviceData = new TestAuditService.TestRequestData(
            data.getTitle(), 
            data.getDescription(), 
            data.getSecretKey(), 
            data.getPriority()
        );
        return testAuditService.testComplexObjectParameters(serviceData);
    }

    public static class TestRequestData {
        private String title;
        private String description;
        private String secretKey;
        private int priority;

        // 생성자 및 getter/setter
        public TestRequestData() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
}
