/**
 * @fileoverview 실시간 페이지 편집/동기화 WebSocket 핸들러
 *
 * 이 파일은 사용자가 워크스페이스/페이지에 입장한 후,
 * 에디터에서 발생하는 편집 이벤트(입력, 삭제, 커서 이동 등)를
 * WebSocket을 통해 서버로 전파하고, 서버는 이를 같은 페이지의 다른 사용자에게 브로드캐스트합니다.
 *
 * 주요 기능:
 * - 클라이언트의 실시간 편집 이벤트 수신 및 검증
 * - 같은 페이지(room)에 속한 사용자에게 편집 내용 브로드캐스트
 * - 권한(role) 체크 및 편집 제한
 * - 충돌 방지 및 동기화(추후 확장)
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Server as SocketIOServer, Socket } from 'socket.io';
import { applicationLogger, withAuditLog } from '../utils/logger';
import { handleUserDisconnectPageWithAudit, handleUserJoinPage, handleUserJoinPageWithAudit } from '../handlers/presence.handler';
import { handleSocketAuth } from '../handlers/auth.handler';
import { BaseMessage } from '../rabbitmq/types/base-message.types';
import { BlockOperation } from '../rabbitmq/types/block-operation.enum';
import { BlockDTO } from '../types/notionCopy/block-dto.types';
import { MessageType } from '../rabbitmq/types/message-type.enum';
import { WorkspaceRole } from '../constants/workspace-role.enum';
import { ERROR_MESSAGES } from '../constants/error-messages';
import { getSocketsByUser, getUsersInPage } from '../services/presence.service';
import { getRoomName, SOCKET_EVENTS } from '../constants/socket-events.constants';
import { emitSocketError, handleError } from '../utils/error-handler';
import { PageEditError } from '../types/error/PageEditError';
import { BlockMessage } from '../rabbitmq/types/block-message.types';
import { publishBlockUpdateMessageToCollaborationExchangeByBroadcast } from '../rabbitmq/rabbitmq.producer';

// 예시: 실제 구현은 추후 작성

/**
 * 감사 로그가 적용된 Block Update 처리 함수
 */
export const handleBlockUpdateBroadcastWithAudit = withAuditLog(
    'BlockUpdateBroadcast',
    handleBlockUpdateBroadcast
);




/**
 * Block Update 이벤트 핸들러
 * 이벤트가 발생하면 해당 페이지의 모든 사용자에게 블록 수정 내용을 브로드캐스트합니다.
 *  @param {SocketIOServer} io - Socket.IO 서버 인스턴스
 *  @param {Socket} socket - 클라이언트 소켓 인스턴스
 *  @param {Object} data - 블록 수정 데이터
 * @typedef {BaseMessage} BlockUpdateData
 *  @property {MessageType} messageType - 메시지 타입
 *  @property {string } messageId - 메시지 고유 식별자
 *  @property {string} timestamp - 이벤트 발생 시간
 *  @property {Record<string, any>} metadata? - 추가 메타데이터 (선택적)
 *  @property {BlockOperation} blockOperation - 블록 수정 작업 정보
 *  @property {BlockDTO} blockDTO - 수정된 블록 데이터
 *  @property {string } workspaceId - 워크스페이스 ID
 *  @property {string} userId - 사용자 ID
 */
export async function handleBlockUpdateBroadcast(
    io : SocketIOServer,
    socket : Socket,
    data : {
        messageType: MessageType,
        messageId: string,
        timestamp: string,
        workspaceId: string,
        blockOperation: BlockOperation,
        blockDTO: BlockDTO,
        userId: string
    } 
)
{

    const methodName : string = 'handleBlockUpdateBroadcast';

    try {

    // 1. 사용자의 워크스페이스 권한 확인

        if (socket.data.workspaceRole === WorkspaceRole.GUEST)
        {
            applicationLogger.warn(`User ${socket.data.userId} attempted to update block in workspace ${data.workspaceId} without sufficient permissions`);
            socket.emit(SOCKET_EVENTS.ERROR, { message: ERROR_MESSAGES.USER_PERMISSIONS_DENIED });
            return;
        }

        // 2. BlockDTO 및 각종 필드 유효성 검사
        // TODO : BlockDTO의 필수 필드 및 형식 검증 로직 추가


        // 3. 현재 동일한 페이지 보고 잇는 사용자들에게 변경 사항 전파
        const roomName : string = getRoomName(data.workspaceId, data.blockDTO.pageId);

        // 현재 페이지에 접속 중인 사용자 목록을 가져옴
        const usersInPage : Set<string> = getUsersInPage(roomName);

        // BlockMessage 형태로 데이터 변환 
        const blockMessage: BlockMessage = {
            messageType: data.messageType,
            messageId: data.messageId,
            timestamp: data.timestamp,
            workspaceId: data.workspaceId,
            blockOperation: data.blockOperation,
            blockDTO: data.blockDTO,
            userId: data.userId
        };

        // 각 사용자에게 블록 수정 이벤트 전송
        usersInPage.forEach(userId => { 

            // 사용자 소켓 ID 목록을 가져옴
            const userSockets : Set<string> = getSocketsByUser(userId);

            userSockets.forEach(socketId => {

                // 자기 자신의 소켓이면 건너뜀
                if (socketId === socket.id) return;


                // 소켓 ID가 정의되지 않은 경우 건너뜀
                if (!socketId)
                {
                    applicationLogger.warn(`Socket ID for user ${userId} is undefined, skipping block update broadcast`);
                    return;
                }

                // 해당 소켓 ID로 사용자 소켓 인스턴스 획득
                const userSocket : Socket | undefined = io.sockets.sockets.get(socketId);

                // 사용자 소켓이 존재하는 경우에만 이벤트 전송
                if (userSocket) {
                    userSocket.emit(SOCKET_EVENTS.BLOCK_UPDATE, {
                        ...blockMessage,
                        
                    }
                );
                }
            })

        });

        // 4. rabbitmq producer 를 통한 블록 수정 메세지 broadcast
        // TODO : RabbitMQ 프로듀서를 통한 블록 수정 메시지 전송 로직 추가
        await publishBlockUpdateMessageToCollaborationExchangeByBroadcast(blockMessage);

        applicationLogger.info(`[${methodName}] Block update broadcasted successfully for block ID ${data.blockDTO.id} in workspace ${data.workspaceId}`);


    } catch (error)
    {
        emitSocketError(
            socket,
            error = {
                code : 'Block Update Broadcast Error' ,
                message: error instanceof Error ? error.message : String(error),
                details: {
                    workspaceId: data.workspaceId,
                }
            }
        );

        // 에러 핸들링
        handleError(new PageEditError(`[${methodName}] ${error instanceof Error ? error.message : String(error)}`), methodName);
    }

}








