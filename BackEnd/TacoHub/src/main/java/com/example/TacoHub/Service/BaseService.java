package com.example.TacoHub.Service;

import lombok.extern.slf4j.Slf4j;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.SystemException;

/**
 * 서비스 클래스들의 공통 기능을 제공하는 기본 클래스
 * 현업 기준 예외 처리 전략: Exception Hierarchy 기반 자동 분류
 */
@Slf4j
public abstract class BaseService {
    
    // ========== 기초 유틸리티 메서드 ==========
    
    /**
     * 객체가 null인지 검사합니다
     * @param obj 검사할 객체
     * @return null이면 true, 아니면 false
     */
    protected boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 객체가 null이 아닌지 검사합니다
     * @param obj 검사할 객체
     * @return null이 아니면 true, null이면 false
     */
    protected boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 문자열이 null이거나 비어있는지 검사합니다
     * @param str 검사할 문자열
     * @return null이거나 비어있으면 true, 아니면 false
     */
    protected boolean isStringNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 문자열이 null이 아니고 비어있지 않은지 검사합니다
     * @param str 검사할 문자열
     * @return null이 아니고 비어있지 않으면 true, 아니면 false
     */
    protected boolean isStringNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 문자열의 길이가 지정된 최대값을 초과하는지 검사합니다
     * @param str 검사할 문자열
     * @param maxLength 최대 길이
     * @return 길이가 초과하면 true, 아니면 false
     */
    protected boolean isStringTooLong(String str, int maxLength) {
        return str != null && str.length() > maxLength;
    }

    /**
     * 문자열의 길이가 지정된 최소값보다 짧은지 검사합니다
     * @param str 검사할 문자열
     * @param minLength 최소 길이
     * @return 길이가 부족하면 true, 아니면 false
     */
    protected boolean isStringTooShort(String str, int minLength) {
        return str == null || str.length() < minLength;
    }

    /**
     * 숫자가 음수인지 검사합니다
     * @param number 검사할 숫자
     * @return 음수이면 true, 아니면 false
     */
    protected boolean isNegative(Number number) {
        return number != null && number.doubleValue() < 0;
    }

    /**
     * 숫자가 0인지 검사합니다
     * @param number 검사할 숫자
     * @return 0이면 true, 아니면 false
     */
    protected boolean isZero(Number number) {
        return number != null && number.doubleValue() == 0;
    }

    /**
     * 숫자가 양수인지 검사합니다
     * @param number 검사할 숫자
     * @return 양수이면 true, 아니면 false
     */
    protected boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    // ========== 예외 처리 메서드 ==========
    
    /**
     * 통합 예외 처리 메서드 (Exception Hierarchy 기반)
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @param customException 전파할 커스텀 예외
     * @param <T> 커스텀 예외 타입
     * @throws T 전달받은 커스텀 예외
     */
    protected <T extends RuntimeException> void handleAndThrow(
            String methodName, 
            Exception originalException, 
            T customException) {
        
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        // Exception Hierarchy 기반 자동 분류
        if (customException instanceof BusinessException) {
            // 비즈니스 예외는 WARN 레벨로 로깅 (스택 트레이스 없음)
            log.warn("[BUSINESS_ERROR] {} 실패: type={}, message={}", 
                    methodName, exceptionType, errorMessage);
        } else if (customException instanceof SystemException) {
            // 시스템 예외는 ERROR 레벨로 로깅 (스택 트레이스 포함)
            log.error("[SYSTEM_ERROR] {} 실패: type={}, message={}", 
                    methodName, exceptionType, errorMessage, originalException);
        } else {
            // 분류되지 않은 예외는 ERROR 레벨로 로깅 (안전한 기본값)
            log.error("[UNKNOWN_ERROR] {} 실패: type={}, message={}", 
                    methodName, exceptionType, errorMessage, originalException);
        }
        
        throw customException;
    }
    
    /**
     * 비즈니스 예외 처리 (명시적 분류)
     * 개발자가 확실히 비즈니스 예외임을 알고 있을 때 사용
     */
    protected <T extends RuntimeException> void handleBusinessException(
            String methodName, 
            Exception originalException, 
            T customException) {
        
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.warn("[BUSINESS_ERROR] {} 실패: type={}, message={}", 
                methodName, exceptionType, errorMessage);
        
        throw customException;
    }
    
    /**
     * 시스템 예외 처리 (명시적 분류)
     * 개발자가 확실히 시스템 예외임을 알고 있을 때 사용
     */
    protected <T extends RuntimeException> void handleSystemException(
            String methodName, 
            Exception originalException, 
            T customException) {
        
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("[SYSTEM_ERROR] {} 실패: type={}, message={}", 
                methodName, exceptionType, errorMessage, originalException);
        
        throw customException;
    }
    
    /**
     * 비즈니스 예외 로깅 및 전파 (의도된 예외)
     * 메서드 내에서 의도적으로 비즈니스 예외를 발생시킬 때 사용
     * 
     * @param context 컨텍스트 정보 (메서드명, 작업 설명 등)
     * @param message 로깅 메시지
     * @param exception 전파할 비즈니스 예외
     * @param <T> 비즈니스 예외 타입
     * @throws T 전달받은 비즈니스 예외
     */
    protected <T extends BusinessException> void logAndThrowBusiness(
            String context, 
            String message, 
            T exception) {
        
        log.warn("[BUSINESS_ERROR] {}: {}", context, message);
        throw exception;
    }

    /**
     * 비즈니스 예외 로깅 및 전파 (매개변수 포함)
     * 디버깅을 위한 매개변수 정보를 포함하여 로깅
     * 
     * @param context 컨텍스트 정보
     * @param message 로깅 메시지
     * @param params 매개변수 정보 (키-값 쌍)
     * @param exception 전파할 비즈니스 예외
     * @param <T> 비즈니스 예외 타입
     * @throws T 전달받은 비즈니스 예외
     */
    protected <T extends BusinessException> void logAndThrowBusiness(
            String context, 
            String message, 
            Object[] params, 
            T exception) {
        
        log.warn("[BUSINESS_ERROR] {}: {}, params={}", context, message, params);
        throw exception;
    }
}
