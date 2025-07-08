package com.example.TacoHub.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObjectMapper 설정 클래스
 * JSON 직렬화/역직렬화에 사용되는 ObjectMapper 빈을 제공
 * 
 * 주요 설정:
 * - JavaTimeModule: LocalDateTime 등 Java 8 시간 API 지원
 * - WRITE_DATES_AS_TIMESTAMPS 비활성화: ISO-8601 형식으로 날짜 출력
 */
@Configuration
public class ObjectMapperConfig {

    /**
     * 공통 ObjectMapper 빈 설정
     * AOP 로깅, API 응답 등에서 사용
     * @return 설정된 ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 시간 API 지원
        mapper.registerModule(new JavaTimeModule());
        
        // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 출력
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 순환 참조 감지 및 처리 (필요시 추가)
        // mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return mapper;
    }
}
