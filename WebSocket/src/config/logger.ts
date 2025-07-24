/**
 * @fileoverview Winston 로깅 시스템 설정
 * 
 * 이 파일은 TacoHub WebSocket 서버의 로깅 시스템을 구성합니다:
 * 1. 구조화된 로그 형식 (JSON) 지원
 * 2. 로그 레벨별 분리 저장 (error, combined)
 * 3. 콘솔 및 파일 출력 동시 지원
 * 4. 개발/프로덕션 환경별 로깅 설정
 * 
 * 실시간 WebSocket 서비스의 디버깅, 모니터링, 트러블슈팅을 위한
 * 체계적인 로깅 인프라를 제공합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import winston from 'winston';
import path from 'path';

/**
 * 로그 레벨 설정
 * 환경변수 LOG_LEVEL로 제어 가능 (error, warn, info, http, verbose, debug, silly)
 */
const logLevel = process.env.LOG_LEVEL || 'info';

/**
 * 로그 저장 디렉토리 설정
 * 프로덕션에서는 별도 볼륨에 저장 권장
 */
const logDir = process.env.LOG_DIR || 'logs';

/**
 * 개발 환경용 로그 형식
 * 가독성을 위해 컬러와 간단한 형식 사용
 */
const developmentFormat = winston.format.combine(
    winston.format.colorize(),
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    winston.format.printf(({ timestamp, level, message, service, ...meta }) => {
        let metaStr = '';
        if (Object.keys(meta).length > 0) {
            metaStr = '\n' + JSON.stringify(meta, null, 2);
        }
        return `${timestamp} [${service}] ${level}: ${message}${metaStr}`;
    })
);

/**
 * 프로덕션 환경용 로그 형식
 * 구조화된 JSON 형식으로 로그 분석 도구와 호환
 */
const productionFormat = winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json(),
    winston.format.metadata({ fillExcept: ['message', 'level', 'timestamp', 'service'] })
);

/**
 * 현재 환경에 따른 로그 형식 선택
 */
const logFormat = process.env.NODE_ENV === 'production' ? productionFormat : developmentFormat;

/**
 * Winston Logger 인스턴스 생성
 * 
 * 설정 내용:
 * - 로그 레벨: 환경변수 또는 기본값 'info'
 * - 서비스 식별자: 'websocket-server'
 * - 다중 출력 대상: 파일(error, combined), 콘솔
 * - 에러 스택 추적 지원
 * - 타임스탬프 자동 추가
 */
export const logger = winston.createLogger({
    level: logLevel,
    format: logFormat,
    defaultMeta: { 
        service: 'websocket-server',
        version: process.env.npm_package_version || '1.0.0',
        environment: process.env.NODE_ENV || 'development'
    },
    transports: [
        /**
         * 에러 로그 파일 출력
         * error 레벨 이상의 로그만 기록
         */
        new winston.transports.File({ 
            filename: path.join(logDir, 'error.log'), 
            level: 'error',
            maxsize: 5 * 1024 * 1024, // 5MB
            maxFiles: 5,               // 최대 5개 파일 로테이션
            tailable: true
        }),
        
        /**
         * 전체 로그 파일 출력
         * 모든 레벨의 로그 기록
         */
        new winston.transports.File({ 
            filename: path.join(logDir, 'combined.log'),
            maxsize: 10 * 1024 * 1024, // 10MB
            maxFiles: 10,               // 최대 10개 파일 로테이션
            tailable: true
        }),
        
        /**
         * 콘솔 출력
         * 개발 시 실시간 로그 확인용
         */
        new winston.transports.Console({
            format: process.env.NODE_ENV === 'production' 
                ? winston.format.simple() 
                : developmentFormat,
            silent: process.env.NODE_ENV === 'test' // 테스트 환경에서는 콘솔 출력 비활성화
        })
    ],
    
    /**
     * 예외 및 거부된 Promise 처리
     * 애플리케이션 크래시 시에도 로그 기록 보장
     */
    exceptionHandlers: [
        new winston.transports.File({ 
            filename: path.join(logDir, 'exceptions.log') 
        })
    ],
    rejectionHandlers: [
        new winston.transports.File({ 
            filename: path.join(logDir, 'rejections.log') 
        })
    ],
    
    /**
     * 예외 발생 시 프로세스 종료 방지
     * 로그 기록 후 계속 실행
     */
    exitOnError: false
});

/**
 * 개발 환경에서 추가 디버깅 정보 제공
 */
if (process.env.NODE_ENV === 'development') {
    logger.debug('Logger initialized in development mode');
    logger.debug(`Log level: ${logLevel}`);
    logger.debug(`Log directory: ${logDir}`);
}

/**
 * 로깅 유틸리티 함수들
 */
export const logUtils = {
    /**
     * WebSocket 이벤트 로깅
     */
    logSocketEvent: (event: string, socketId: string, data?: any) => {
        logger.info('Socket Event', {
            event,
            socketId,
            data: data ? JSON.stringify(data) : undefined,
            timestamp: new Date().toISOString()
        });
    },
    
    /**
     * API 요청 로깅
     */
    logApiRequest: (method: string, url: string, statusCode: number, duration: number) => {
        logger.http('API Request', {
            method,
            url,
            statusCode,
            duration,
            timestamp: new Date().toISOString()
        });
    },
    
    /**
     * 사용자 활동 로깅
     */
    logUserActivity: (userId: string, activity: string, details?: any) => {
        logger.info('User Activity', {
            userId,
            activity,
            details,
            timestamp: new Date().toISOString()
        });
    },
    
    /**
     * 성능 메트릭 로깅
     */
    logPerformance: (metric: string, value: number, unit: string) => {
        logger.verbose('Performance Metric', {
            metric,
            value,
            unit,
            timestamp: new Date().toISOString()
        });
    }
};
