/**
 * @fileoverview WebSocket 연결 관리 핸들러
 * 
 * 이 파일은 Socket.IO 클라이언트의 연결/해제를 관리하는 핵심 핸들러입니다:
 * 1. 클라이언트 연결 시 인증 및 초기화 처리
 * 2. 연결 해제 시 정리 작업 및 상태 동기화
 * 3. 사용자 온라인/오프라인 상태 관리
 * 4. 기본적인 소켓 이벤트 처리 및 응답
 * 
 * 모든 WebSocket 통신의 시작점이며, 사용자 세션 관리를 담당합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Server as SocketIOServer, Socket } from 'socket.io';
import { applicationLogger } from '../utils/logger';
import { handleUserDisconnectPageWithAudit, handleUserJoinPage, handleUserJoinPageWithAudit } from '../handlers/presence.handler';
import { handleSocketAuth } from '../handlers/auth.handler';
import {  handleBlockUpdateBroadcastWithAudit,  } from '../handlers/page-edit.handler';
import { SOCKET_EVENTS } from '../constants/socket-events.constants';
import type { SocketErrorPayload, SocketEventMap, SocketResponse,  } from '../types/socket-events.types';
import { handleError, emitSocketError, emitSocketSuccess } from '../utils/error-handler';


/**
 * 연결 핸들러 설정 함수
 * Socket.IO 서버에 연결 관련 이벤트 리스너를 등록합니다
 * 
 * @function setupConnectionHandler
 * @param {SocketIOServer} io - Socket.IO 서버 인스턴스
 * 
 * 주요 기능:
 * - 클라이언트 연결/해제 이벤트 처리
 * - 기본적인 소켓 에러 처리
 * - 연결 성공 응답 전송
 */
export function setupConnectionHandler(io: SocketIOServer) {
    /**
     * 클라이언트 연결 이벤트 핸들러
     * 새로운 클라이언트가 WebSocket에 연결될 때 실행됩니다
     */
    io.on(SOCKET_EVENTS.CONNECTION, async (socket: Socket) => {
        applicationLogger.info(`Client connected: ${socket.id}`);

        // === 기본 이벤트 핸들러 등록 ===
        
        /**
         * 클라이언트 인증 및 초기화 처리
         */
        const isAuth = await handleSocketAuth(socket);
        if (!isAuth) 
            {
            applicationLogger.warn(`Client ${socket.id} failed authentication`);
            socket.disconnect(); // 인증 실패 시 연결 종료
            return;
        }

        /**
         * 클라이언트 실시간 사용자 등록 이벤트
         */
        socket.on(
            SOCKET_EVENTS.PAGE_JOIN,
            async (data: Parameters<SocketEventMap[typeof SOCKET_EVENTS.PAGE_JOIN]>[0]) => 
                await handleUserJoinPageWithAudit(socket, data)
        );


        /**
         * Workspace page의 block 수정 이벤트(사용자 수정 후, 다른 모든 곳으로 Broadcast)
         */
        socket.on(
            SOCKET_EVENTS.BLOCK_UPDATE_BROADCAST,
            async (data: Parameters<SocketEventMap[typeof SOCKET_EVENTS.BLOCK_UPDATE_BROADCAST]>[0]) => 
                await handleBlockUpdateBroadcastWithAudit(io, socket, data)
               
        );


        /**
         * 클라이언트 연결 해제 이벤트 처리
         * 클라이언트가 의도적으로 또는 네트워크 문제로 연결을 끊을 때 실행
         */
        socket.on(
            SOCKET_EVENTS.DISCONNECT,
            async () => {
                // TODO: 향후 확장 시 다음 작업들 수행
                // - 사용자 오프라인 상태를 다른 클라이언트에게 알림
                // - 참가 중인 워크스페이스/페이지에서 제거
                // - 세션 데이터 정리 (Redis 등)
                await handleUserDisconnectPageWithAudit(socket);
            }
        );
        
        /**
         * Socket 에러 이벤트 처리
         * 소켓 통신 중 발생하는 오류를 캐치하고 로깅
         */
        socket.on(
            SOCKET_EVENTS.ERROR,
            (error: any) => 
            {
                emitSocketError(
                    socket, 
                    error = 
                    {
                        code: error.code || 'SOCKET_ERROR',
                        message: error.message || 'An unknown socket error occurred',
                        details: error.details || {}, // 추가 정보 (디버깅용)
                    }
                );
            }
        );
        
        // === 연결 완료 응답 ===
        
        /**
         * 클라이언트에게 연결 성공 알림 전송
         * 클라이언트가 연결 상태를 확인할 수 있도록 초기 응답 제공
         */
        emitSocketSuccess(socket, SOCKET_EVENTS.CONNECTED, {
            socketId: socket.id
        });
    });
}
