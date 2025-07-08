package com.example.TacoHub.Logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 파라미터 처리 유틸리티 클래스
 * 
 * 이 클래스는 AOP를 통해 캡처된 메서드 파라미터들을 안전하게 처리합니다:
 * 1. 파라미터명과 값을 Map으로 매핑
 * 2. 민감정보 자동 마스킹 (password, token 등)
 * 3. 복잡한 객체의 JSON 직렬화
 * 4. 순환 참조 방지
 * 5. 직렬화 실패 시 fallback 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ParameterProcessor {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 메서드 파라미터를 Map 형태로 추출
     * @param signature 메서드 시그니처
     * @param args 실제 파라미터 값들
     * @return 파라미터명-값 Map
     */
    public Map<String, Object> extractParameters(MethodSignature signature, Object[] args) {
        Map<String, Object> parameters = new HashMap<>();
        
        try {
            String[] parameterNames = signature.getParameterNames();
            
            if (parameterNames != null && args != null) {
                for (int i = 0; i < Math.min(parameterNames.length, args.length); i++) {
                    String paramName = parameterNames[i];
                    Object paramValue = args[i];
                    
                    // 민감정보 마스킹 처리
                    Object maskedValue = maskSensitiveData(paramName, paramValue);
                    
                    parameters.put(paramName, maskedValue);
                }
            }
        } catch (Exception e) {
            log.warn("파라미터 추출 실패: {}", e.getMessage());
        }
        
        return parameters;
    }
    
    /**
     * 객체를 JSON 문자열로 안전하게 직렬화
     * 순환 참조와 직렬화 실패를 방지하여 안전하게 로깅
     * @param obj 직렬화할 객체
     * @return JSON 문자열 (실패시 toString() 결과)
     */
    public String serializeToJson(Object obj) {
        if (obj == null) return null;
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 시 fallback으로 toString() 사용
            log.debug("JSON 직렬화 실패, toString() 사용: {}", e.getMessage());
            return obj.toString();
        } catch (Exception e) {
            // 기타 예외 발생 시 안전한 문자열 반환
            log.debug("객체 직렬화 중 예외 발생: {}", e.getMessage());
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }
    
    /**
     * 민감정보 마스킹 처리
     * 파라미터명을 기반으로 민감정보를 자동 감지하고 마스킹
     * @param paramName 파라미터명
     * @param paramValue 파라미터값
     * @return 마스킹 처리된 값
     */
    public Object maskSensitiveData(String paramName, Object paramValue) {
        if (paramValue == null) {
            return null;
        }
        
        // 민감정보 파라미터인지 확인
        if (isSensitiveParameter(paramName)) {
            return "***MASKED***";  // 민감정보는 명확한 마스킹 표시
        }
        
        // 기본 타입은 그대로 반환
        if (isPrimitiveType(paramValue)) {
            return paramValue;
        }
        
        // 복잡한 객체는 JSON으로 직렬화 (순환 참조 방지)
        return serializeToJson(paramValue);
    }
    
    /**
     * 기본 타입 여부 확인
     * @param obj 확인할 객체
     * @return 기본 타입 여부
     */
    private boolean isPrimitiveType(Object obj) {
        return obj instanceof String || 
               obj instanceof Number || 
               obj instanceof Boolean ||
               obj instanceof Character;
    }
    
    /**
     * 파라미터명이 민감정보인지 확인
     * 다양한 민감정보 패턴을 검사하여 자동으로 마스킹 대상 식별
     * @param paramName 파라미터명
     * @return 민감정보 여부
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) {
            return false;
        }
        
        String lowerParamName = paramName.toLowerCase();
        
        // 민감정보 키워드 패턴들
        String[] sensitivePatterns = {
            "password", "pwd", "passwd",      // 비밀번호
            "token", "jwt", "auth",           // 토큰류
            "secret", "key", "private",       // 비밀키
            "credential", "cert",             // 인증서
            "ssn", "social", "personalnumber", // 주민번호
            "card", "account", "bankaccount", // 카드/계좌번호
            "pin", "code", "otp"             // PIN/인증코드

        };
        
        for (String pattern : sensitivePatterns) {
            if (lowerParamName.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 파라미터 맵의 크기 제한
     * 너무 많은 파라미터가 있는 경우 로그 크기 제한
     * @param parameters 원본 파라미터 맵
     * @return 크기 제한된 파라미터 맵
     */
    public Map<String, Object> limitParameterSize(Map<String, Object> parameters) {
        final int MAX_PARAMS = 20; // 최대 파라미터 개수
        
        if (parameters == null || parameters.size() <= MAX_PARAMS) {
            return parameters;
        }
        
        Map<String, Object> limitedParams = new HashMap<>();
        int count = 0;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (count >= MAX_PARAMS) {
                limitedParams.put("...", "추가 " + (parameters.size() - MAX_PARAMS) + "개 파라미터 생략");
                break;
            }
            limitedParams.put(entry.getKey(), entry.getValue());
            count++;
        }
        
        return limitedParams;
    }
    
}
