
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

// Winston 로깅 패키지 및 기타 유틸리티 임포트
import winston from 'winston';
import path from 'path';
import WinstonCloudwatch from 'winston-cloudwatch';

// AWS SSM Package
import { getSecret } from './aws';
import { AuditLog } from "../audit/auditLog.types";
import {
  extractEventInfo,
  extractUserInfo,
  extractNetworkInfo,
  extractParameters,
  extractResult,
  extractErrorInfo,
  extractPerformance,
  extractTimestamp
} from "../audit/auditLog.extractor";
import { UploadAuditLogToS3 } from "./aws";

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
let auditLogger : winston.Logger;
let applicationLogger : winston.Logger;
const auditLoggerTransports : winston.transport[] = [];
const applicationLoggerTransports : winston.transport[] = [];


/**
 * Winston Logger Instance 생성 및 초기화
 */
export async function initLogger()
{

    /**
     * 현재 환경에 따른 CloudWatch Log Group 설정
     */
    const ApplicationLogGroup = await getSecret(`cloudwatch/log-group/application`);
    const AuditLogGroup = await getSecret(`cloudwatch/log-group/audit`);
    const ErrorLogGroup = await getSecret(`cloudwatch/log-group/error`);
    const InfoLogGroup = await getSecret(`cloudwatch/log-group/info`);

    // Local / Test&Prod 환경에 따라 CloudWatch 설정
    if (process.env.NODE_ENV === 'local')
    {
        // Application Logger: 파일 + 콘솔
        applicationLoggerTransports.push(
            new winston.transports.File({
            filename: path.join(logDir, 'application.log'),
            level: 'info',
            format: logFormat
            }),
            new winston.transports.Console({
            format: logFormat
            })
        );

        // Audit Logger: 파일 + 콘솔
        auditLoggerTransports.push(
            new winston.transports.File({
            filename: path.join(logDir, 'audit.log'),
            level: 'info',
            format: logFormat
            }),
            new winston.transports.Console({
            format: logFormat
            })
        );
        
    }


    if (process.env.NODE_ENV === 'prod' || process.env.NODE_ENV === 'test')
    {
        // Application Logger: CloudWatch
        applicationLoggerTransports.push(
            new WinstonCloudwatch({
            logGroupName: ApplicationLogGroup, // SSM에서 받아온 값
            logStreamName: `${process.env.NODE_ENV}-application-${new Date().toISOString().split('T')[0]}`,
            awsRegion: process.env.AWS_REGION,
            awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
            awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
            level: 'info', // info 이상만 기록
            jsonMessage: true
            })
        );

        // Audit Logger: CloudWatch
        auditLoggerTransports.push(
            new WinstonCloudwatch({
            logGroupName: AuditLogGroup,
            logStreamName: `${process.env.NODE_ENV}-audit-${new Date().toISOString().split('T')[0]}`,
            awsRegion: process.env.AWS_REGION,
            awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
            awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
            level: 'info', // 감사 로그는 info 이상
            jsonMessage: true
            })
        );
    }

    // Logger 인스턴스 생성
    applicationLogger = winston.createLogger({
        level: logLevel,
        format: logFormat,
        defaultMeta: { service: 'websocket-server', type: 'application' },
        transports: applicationLoggerTransports,
        exceptionHandlers : [
            new WinstonCloudwatch({
            logGroupName: ErrorLogGroup,
            logStreamName: `${process.env.NODE_ENV}-error-${new Date().toISOString().split('T')[0]}`,
            awsRegion: process.env.AWS_REGION,
            awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
            awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
            level: 'error',
            jsonMessage: true
            })
        ],
        rejectionHandlers : [
            new WinstonCloudwatch({
            logGroupName: ErrorLogGroup,
            logStreamName: `${process.env.NODE_ENV}-rejection-${new Date().toISOString().split('T')[0]}`,
            awsRegion: process.env.AWS_REGION,
            awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
            awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
            level: 'error',
            jsonMessage: true
            })
        ],
        exitOnError: false // 예외 발생 시 프로세스 종료 방지
    });

    auditLogger = winston.createLogger({
        level: logLevel,
        format: logFormat,
        defaultMeta: { service: 'websocket-server', type: 'audit' },
        transports: auditLoggerTransports,
        exceptionHandlers: [
            new WinstonCloudwatch({
                logGroupName: ErrorLogGroup,
                logStreamName: `${process.env.NODE_ENV}-error-${new Date().toISOString().split('T')[0]}`,
                awsRegion: process.env.AWS_REGION,
                awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
                awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
                level: 'error',
                jsonMessage: true
            })
        ],
        rejectionHandlers: [
            new WinstonCloudwatch({
                logGroupName: ErrorLogGroup,
                logStreamName: `${process.env.NODE_ENV}-rejection-${new Date().toISOString().split('T')[0]}`,
                awsRegion: process.env.AWS_REGION,
                awsAccessKeyId: process.env.AWS_ACCESS_KEY_ID,
                awsSecretKey: process.env.AWS_SECRET_ACCESS_KEY,
                level: 'error',
                jsonMessage: true
            })
        ],
        exitOnError: false // 예외 발생 시 프로세스 종료 방지
    });

}


/**
 * Audit Logging 데코레이터
 * 메서드 실행 시 감사 로그를 자동 기록하고 S3에 저장
 */
export function AuditLogDecorator(eventName: string) {
    return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) 
    {
        
        // 원본 메서드 저장
        const originalMethod = descriptor.value;

        // 데코레이터가 적용된 메서드에 감사 로그 기능 추가
        descriptor.value = async function (...args: any[]) {

            // 감사 로그 정보 초기화
            const start = Date.now();

            // 감사 로그 객체 생성
            let auditLog: AuditLog = {
                ...extractEventInfo(eventName, propertyKey),
                ...extractUserInfo(this),
                ...extractNetworkInfo(this),
                ...extractParameters(args),
                ...extractTimestamp()
            };

            // 감사 로그 기록 시작
            let result, error; // 결과 및 오류 변수 초기화

            try {
                // 원본 메서드 실행
                result = await originalMethod.apply(this, args);

                // 감사 로그 업데이트
                auditLog = {
                ...auditLog,
                ...extractResult(result),
                ...extractPerformance(start, Date.now()),
                };

                // 오류 정보 초기화 (성공 시)
                auditLog.errorType = undefined;
                auditLog.errorMessage = undefined;
                auditLog.stackTrace = undefined;
                auditLog = { ...auditLog, ...extractTimestamp() };
                auditLogger.info("[AUDIT]", auditLog);
            } 
            catch (err) 
            {
                // 오류 발생 시 감사 로그 업데이트
                error = err;

                // 감사 로그에 오류 정보 추가
                auditLog = {
                ...auditLog,
                ...extractErrorInfo(err),
                ...extractPerformance(start, Date.now()),
                };

                // 오류 정보 초기화
                auditLog = { ...auditLog, ...extractTimestamp() };
                auditLogger.error("[AUDIT]", auditLog);
                throw err;
            } 
            finally 
            {
                // S3 저장: key는 'audit/{날짜}/{이벤트명}-{userId}-{timestamp}.json' 형태
                const dateStr = auditLog.timestamp?.slice(0, 10) || "unknown";
                const userIdStr = auditLog.userId || "anonymous";
                const key = `audit/${dateStr}/${eventName}-${userIdStr}-${auditLog.timestamp.replace(/[:.]/g,"")}.json`;
                await UploadAuditLogToS3(key, JSON.stringify(auditLog));
            }
            return result;
        };
    };
}


/**
 * Audit Logging 고차 함수 래퍼
 * - 데코레이터 없이 함수형 코드에 감사 로그 AOP 적용 가능
 * @param eventName 감사 이벤트명
 * @param fn 래핑할 async 함수
 * @returns 감사 로그가 적용된 async 함수
 */
export function withAuditLog<T extends (...args: any[]) => Promise<any>>(
  eventName: string,
  fn: T
): T {
    return (async function(this: any, ...args: any[]) {

        // 감사 로그 정보 초기화
        const start = Date.now();
        let auditLog: AuditLog = {
            ...extractEventInfo(eventName, fn.name),
            ...extractUserInfo(this),
            ...extractNetworkInfo(this),
            ...extractParameters(args),
            ...extractTimestamp()
        };

        // 감사 로그 기록 시작
        let result;


        try {   
        // 원본 함수 실행
        result = await fn.apply(this, args);
        auditLog = {
            ...auditLog,
            ...extractResult(result),
            ...extractPerformance(start, Date.now()),
        };

        auditLog.errorType = undefined;
        auditLog.errorMessage = undefined;
        auditLog.stackTrace = undefined;
        auditLog = { ...auditLog, ...extractTimestamp() };

        auditLogger.info("[AUDIT]", auditLog);

        } 
        catch (err) 
        {
        // 오류 발생 시 감사 로그 업데이트
        auditLog = {
            ...auditLog,
            ...extractErrorInfo(err),
            ...extractPerformance(start, Date.now()),
        };

        auditLog = { ...auditLog, ...extractTimestamp() };


        auditLogger.error("[AUDIT]", auditLog);
        throw err;
    } 
    finally 
    {
        
        // S3 저장: key는 'audit/{날짜}/{이벤트명}-{userId}-{timestamp}.json' 형태
        const dateStr = auditLog.timestamp?.slice(0, 10) || "unknown";
        const userIdStr = auditLog.userId || "anonymous";
        const key = `audit/${dateStr}/${eventName}-${userIdStr}-${auditLog.timestamp.replace(/[:.]/g,"")}.json`;
        await UploadAuditLogToS3(key, JSON.stringify(auditLog));
    }
    return result;
  }) as T;
}



export {applicationLogger, auditLogger};