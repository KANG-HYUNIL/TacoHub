/**
 * @fileoverview WebSocket 인증 핸들러
 *
 * 이 파일은 WebSocket 연결 시 클라이언트가 보낸 JWT Access Token의 유효성 검증 및 사용자 인증을 담당합니다.
 * - 최초 connection 시 토큰 검증 및 사용자 정보 추출
 * - 인증 실패 시 소켓 연결 거부 및 에러 메시지 전송
 * - 인증 성공 시 socket.data.auth 등에 사용자 정보 저장
 * - 워크스페이스 이동 등 추가 인증 로직 확장 가능
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Socket } from 'socket.io';
import { isValidAccessToken, isExpiredAccessToken } from '../services/auth.service';
import { ERROR_MESSAGES } from '../constants/error-messages';
import { SocketResponse } from '../types/socket-events.types';
import { SOCKET_EVENTS } from '../constants/socket-events.constants';
import { emitSocketError } from '../utils/error-handler';


/**
 * WebSocket 인증 처리 핸들러
 * @param socket - 클라이언트 소켓
 * @param accessToken - 클라이언트가 보낸 JWT Access Token
 * @returns 인증 성공 시 true, 실패 시 false
 */
export async function handleSocketAuth(socket: Socket): Promise<boolean> {

    const accessToken = socket.handshake.auth.token;

    // 1. 토큰 유효성 검증


    if (!accessToken || !isValidAccessToken(accessToken)) {
        emitSocketError(socket, {
            code: ERROR_MESSAGES.INVALID_TOKEN,
            message: ERROR_MESSAGES.INVALID_TOKEN,
            details: { socketId: socket.id }
        });
        socket.disconnect();
        return false;
    }

    // 2. 만료 여부 체크
    if (isExpiredAccessToken(accessToken)) {
        emitSocketError(socket, {
            code: ERROR_MESSAGES.EXPIRED_TOKEN,
            message: ERROR_MESSAGES.EXPIRED_TOKEN,
            details: { socketId: socket.id }
        });
        socket.disconnect();
        return false;
    }

    // 3. 인증 실패 시 에러 메시지 emit 및 disconnect

    


    // 4. 인증 성공 시 socket.data.auth 등에 사용자 정보 저장
    // TODO: 실제 구현 필요

    
    return true;
}
