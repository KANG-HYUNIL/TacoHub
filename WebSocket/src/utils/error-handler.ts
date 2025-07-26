import { applicationLogger } from './logger';
import { BusinessError } from '../types/error/BusinessError';
import { ServerError } from '../types/error/ServerError';
import { RabbitMQError } from '../types/error/RabbitMQError';
import { PresenceError } from '../types/error/PresenceError';
import { PageEditError } from '../types/error/PageEditError';
import { Server as SocketIOServer, Socket } from 'socket.io';
import { SocketErrorPayload } from '../types/socket-events.types';
import { SOCKET_EVENTS } from '../constants/socket-events.constants';

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

    // PresenceError 처리
    if (error instanceof PresenceError) {
        applicationLogger.error(`[Presence] ${context ? context + ': ' : ''}${error.message}`);
        throw error;
    }

    // PageError 처리
    if (error instanceof PageEditError) {
        applicationLogger.error(`[Page] ${context ? context + ': ' : ''}${error.message}`);
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



/**
 * Socket.IO 에러 핸들러
 * 소켓 통신 중 발생하는 에러를 처리하고, 클라이언트에게
 * 에러 메시지를 전송합니다.
 * @param {Socket} socket - 소켓 인스턴스
 * @param {SocketErrorPayload} error - 에러 정보
 * @description 
 * 이 함수는 소켓 통신 중 발생하는 에러를 클라이언트에게
 * 전송합니다. 에러 정보는 `SocketErrorPayload` 타입을 따르며,
 * 클라이언트는 이 정보를 통해 에러를 처리할 수 있습니다.
 */

export function emitSocketError(socket : Socket, error : SocketErrorPayload)
{
    applicationLogger.error(`Socket Error for ${socket.id}:`, error);

    socket.emit(SOCKET_EVENTS.ERROR,
        {
            success : false,
            socketId: socket.id,
            timestamp: new Date().toISOString(),
            error: {
                code: error.code || 'SOCKET_ERROR',
                message: error.message || 'An unknown socket error occurred',
                details: error.details || {}, // 추가 정보 (디버깅용)
            } satisfies SocketErrorPayload
        }
    )

}