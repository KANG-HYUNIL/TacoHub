/**
 * @fileoverview 실시간 사용자 presence(접속/퇴장/상태) 관리 핸들러
 *
 * 이 파일은 WebSocket 연결 이후, 사용자의 워크스페이스/페이지 입장, 퇴장, 온라인/오프라인 상태를 관리합니다.
 * - 사용자의 입장/퇴장 이벤트 처리
 * - 현재 접속 중인 사용자 목록 관리
 * - 페이지/워크스페이스별 사용자 상태 동기화 및 브로드캐스트
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Server as SocketIOServer, Socket } from 'socket.io';
import { applicationLogger, auditLogger, AuditLogDecorator, withAuditLog } from '../utils/logger';
import { emitSocketError, handleError } from '../utils/error-handler';
import { PresenceError } from '../types/error/PresenceError';
import { addUserToPage, removeUserFromPage, getUsersInPage } from '../services/presence.service'
import { ERROR_MESSAGES } from '../constants/error-messages';
import { fetchWorkspaceRole } from '../services/spring-api.service';
import { getRoomName } from '../constants/socket-events.constants';
import { WorkspaceRole } from '../constants/workspace-role.enum';


// --- 감사 로그 AOP가 적용된 고차 함수 래핑 버전 ---
/**
 * 감사 로그가 적용된 사용자 페이지 입장 처리 함수
 */
export const handleUserJoinPageWithAudit = withAuditLog(
  'UserJoinPage',
  handleUserJoinPage
);

/**
 * 감사 로그가 적용된 사용자 페이지 퇴장 처리 함수
 */
export const handleUserDisconnectPageWithAudit = withAuditLog(
  'UserDisconnectPage',
  handleUserDisconnectPage
);



/**
 * 사용자 페이지 입장 처리 메서드
 * @param socket - 클라이언트 소켓
 * @param data - 페이지 입장 정보
 * @typedef {Object} PageJoinData
 * @property {string} workspaceId - 워크스페이스 ID
 * @property {string} pageId - 페이지 ID
 * @property {string} userId - 사용자 ID (필수, 클라이언트에서 제공)
 */
export async function handleUserJoinPage(
    socket: Socket, 
    data: 
    { 
        workspaceId: string, 
        pageId: string, 
        userId: string 
    }) 
{
    // TODO: 사용자 입장 처리 로직 구현
    // 1. 사용자 정보를 기반으로 현재 페이지에 등록
    // 2. 다른 클라이언트에게 사용자 입장 알림
    // 3. 사용자 상태 업데이트 (온라인)

    const methodName = 'handleUserJoinPage';

    try {

        applicationLogger.info(`${methodName} - User joining page`, { data });

        // 1. Socket Id 획득
        const socketId = socket.id;
        applicationLogger.info(`${methodName} - Socket ID: ${socketId}`);


        // 2. handshake 데이터 처리
        const { workspaceId, pageId, userId } = data;

        // 유효성 검사
        if (!workspaceId) {
            throw new PresenceError(ERROR_MESSAGES.INVALID_WORKSPACE_ID);
        }

        if (!pageId)
        {
            throw new PresenceError(ERROR_MESSAGES.INVALID_PAGE_ID);
        }

        if (!userId)
        {
            throw new PresenceError(ERROR_MESSAGES.INVALID_USER_ID);
        }


        // Room 이름 생성
        const roomName : string = getRoomName(workspaceId, pageId);


        // 3. 사용자 정보를 기반으로 현재 페이지에 등록

        // 다른 페이지에 연결되어 있는 경우 처리
        if (socket.data.workspaceId && socket.data.pageId && socket.data.userId) 
        {
            // 현재 페이지에서 제거
            const curRoomName : string = getRoomName(socket.data.workspaceId, socket.data.pageId);
            removeUserFromPage(curRoomName, socket.data.userId, socket.id);
        }


        // 만약 WorkSpace 이동이면, 해당 사용자와 Workspace 에서의 권한을 재검증 해야 함
        // TODO : 권한 재검증 로직 추가 필요
        if (!socket.data.workspaceId || socket.data.workspaceId !== workspaceId) 
        {
            const newRole : WorkspaceRole | null = await fetchWorkspaceRole(workspaceId, userId);

            if (newRole) {
                socket.data.workspaceRole = newRole;
            }
        }


        // 현재 페이지에 사용자 등록
        socket.data.workspaceId = workspaceId;
        socket.data.pageId = pageId;
        socket.data.userId = userId;
        
        
        // 페이지 룸에 참가
        socket.join(roomName);
        addUserToPage(roomName, userId, socketId);

        applicationLogger.info(`${methodName} - User ${userId || 'unknown'} joined page ${pageId} in workspace ${workspaceId}`);

    } catch (error)
    {
        emitSocketError(
            socket,
            error = {
                code : 'User Join Page Error',
                message: error instanceof Error ? error.message : String(error),
                details: {
                    workspaceId: data.workspaceId,
                    pageId: data.pageId,
                    userId: data.userId
                }
            }
        );

        // 에러 핸들링
        handleError(new PresenceError(`[${methodName}]  ${error instanceof Error ? error.message : String(error)}`), methodName); // 필요시 상위 호출로 에러 전달
    }




}


/**
 * 사용자 페이지 퇴장 처리 메서드
 * @param socket - 클라이언트 소켓
 * @param data - 페이지 퇴장 정보
 * @typedef {Object} PageLeaveData
 * @property {string} workspaceId - 워크스페이스 ID
 * @property {string} pageId - 페이지 ID
 * @property {string} [userId] - 사용자 ID 
 */
export async function handleUserDisconnectPage(

    socket: Socket
) 
{
    const methodName = 'handleUserDisconnectPage';

    try {

        applicationLogger.info(`${methodName} - User disconnecting from page`);


        // 1. Socket Id 획득
        const socketId = socket.id;
        applicationLogger.info(`${methodName} - Socket ID: ${socketId}`);


        // 2. handshake 데이터 처리
        const { workspaceId, pageId, userId } = socket.data;

        // Room 이름 생성
        const roomName : string = getRoomName(workspaceId, pageId);


        // 3. 사용자 정보를 기반으로 현재 페이지에서 제거
        socket.leave(roomName);
        removeUserFromPage(roomName, userId, socketId);

        applicationLogger.info(`${methodName} - User ${userId || 'unknown'} disconnected from page ${pageId} in workspace ${workspaceId}`);


    } catch (error)
    {
        emitSocketError(
            socket,
            error = {
                code : 'User Disconnect Page Error',
                message: error instanceof Error ? error.message : String(error),

            }

        )

        // 에러 핸들링
        handleError(new PresenceError(`[${methodName}] ${error instanceof Error ? error.message : String(error)}`), methodName);
    }

}
