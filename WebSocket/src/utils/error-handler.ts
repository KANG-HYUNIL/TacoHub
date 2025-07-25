import { applicationLogger } from './logger';
import { BusinessError } from '../types/error/BusinessError';
import { ServerError } from '../types/error/ServerError';
import { RabbitMQError } from '../types/error/RabbitMQError';

/**
 * 에러 핸들러 유틸리티
 * 이 함수는 다양한 에러 타입을 처리하고,
 * 중앙집중식 로깅 시스템에 에러 메시지를 기록합니다.
 * 
 * 사용 예시:
 * ```typescript
 * try {
 *   // Some operation that may throw an error
 * } catch (error) {
 *   handleError(error, 'SomeContext');
 * }
 * 
 * @param {unknown} error - 처리할 에러 객체
 * @param {string} [context] - 에러 발생 컨텍스트 (선택
 * 적)
 * @returns {never} - 항상 예외를 던집니다.
 * @throws {Error} - 처리된 에러를 던집니다.
 * @description
 * 이 함수는 에러 객체의 타입을 검사하고, 해당 타입에 맞는 로
 * 깅을 수행합니다.
 * - `RabbitMQError`: RabbitMQ 관련 에러
 * - `BusinessError`: 비즈니스 로직 에러
 * - `ServerError`: 서버 에러
 * - `Error`: 일반 에러
 * 각 에러 타입에 따라 적절한 로그 메시지를 기록하고,
 * 필요시 추가 정보를 포함할 수 있습니다.   
 * 
 */
export function handleError(error: unknown, context?: string): never {

    // RabbitMQError 처리
    if (error instanceof RabbitMQError) {
        applicationLogger.error(`[RabbitMQ] ${context ? context + ': ' : ''}${error.message}`);
        throw error;
    }

    // BusinessError 처리
    if (error instanceof BusinessError) {
        applicationLogger.error(`[Business] ${context ? context + ': ' : ''}${error.message}`);
        throw error;
    }

    // ServerError 처리
    if (error instanceof ServerError) {
        applicationLogger.error(`[Server] ${context ? context + ': ' : ''}${error.message}`);
        throw error;
    }

    // 일반 Error 처리
    if (error instanceof Error) {
        applicationLogger.error(`[Unknown] ${context ? context + ': ' : ''}${error.message}`);
        throw error;
    }

    // 기타 타입의 에러 처리
    applicationLogger.error(`[Unknown] ${context ? context + ': ' : ''}${String(error)}`);
    throw new Error(String(error));
}
