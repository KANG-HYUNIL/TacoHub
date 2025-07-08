package com.example.TacoHub.Logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로깅을 위한 커스텀 어노테이션
 * 메서드나 클래스에 붙여서 자동 로깅 기능을 활성화
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 메서드와 클래스에 사용 가능
@Retention(RetentionPolicy.RUNTIME)              // 런타임에 어노테이션 정보 유지
public @interface AuditLogging {
    
    /**
     * 액션명 (기본값: 메서드명 사용)
     */
    String action() default "";
    
    /**
     * 파라미터 로깅 포함 여부
     */
    boolean includeParameters() default true;
    
    /**
     * 반환값 로깅 포함 여부
     */
    boolean includeReturnValue() default false;
    
    /**
     * 성능(실행시간) 측정 포함 여부
     */
    boolean includePerformance() default true;
    
    /**
     * 사용자 정보 포함 여부
     */
    boolean includeUserInfo() default true;
    
    /**
     * 에러 상세 정보 포함 여부
     */
    boolean includeErrorDetails() default true;
    
}
