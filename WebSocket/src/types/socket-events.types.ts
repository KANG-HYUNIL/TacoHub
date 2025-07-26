/**
 * @fileoverview Socket.IO 이벤트 타입 정의
 * 
 * 이 파일은 TacoHub WebSocket 서버에서 사용되는 모든 Socket.IO 이벤트의 타입을 정의합니다:
 * 1. 클라이언트-서버 간 이벤트 타입 안전성 보장
 * 2. TypeScript 자동완성 및 컴파일 타임 오류 검출
 * 3. API 문서화 및 유지보수성 향상
 * 4. Frontend와 Backend 간 이벤트 명세 통일
 * 
 * 모든 이벤트는 명명 규칙을 따르며, 도메인별로 네임스페이스가 분리되어 있습니다.
 * (connection, workspace, page, cursor, notification 등)
 * 

/**
 * Socket.IO 서버 이벤트 타입 (서버 내부용)
 * 서버에서만 사용되는 내부 이벤트들을 정의
 */

import { Socket } from "socket.io";
import { SOCKET_EVENTS } from "../constants/socket-events.constants";
import { BlockOperation } from "../rabbitmq/types/block-operation.enum";
import { MessageType } from "../rabbitmq/types/message-type.enum";
import { BlockDTO } from "./notionCopy/block-dto.types";


type SocketEventName = typeof SOCKET_EVENTS[keyof typeof SOCKET_EVENTS];


/**
 * Socket.IO 클라이언트 이벤트 타입 (클라이언트 내부용)
 */
export interface SocketEventMap{

    // CONNECTION 이벤트
    [SOCKET_EVENTS.CONNECTION]: () => void; 

    // DISCONNECTION 이벤트
    [SOCKET_EVENTS.DISCONNECT]: () => void;


    // CONNECTED 이벤트
    // TODO : FIX
    [SOCKET_EVENTS.CONNECTED]: () => void;

    // PAGE JOIN 이벤트
    // TODO :FIX
    [SOCKET_EVENTS.PAGE_JOIN]: 
        (data:
            {
                workspaceId: string; // 워크스페이스 ID
                pageId: string;      // 페이지 ID
                userId: string;      // 사용자 ID
            }
    ) => void;


    // BLOCK UPDATE BROADCAST 이벤트
    // TODO : FIX
    [SOCKET_EVENTS.BLOCK_UPDATE_BROADCAST]: 
        (data: 
            {
                messageType: MessageType,
                messageId: string,
                timestamp: string,
                workspaceId: string,
                blockOperation: BlockOperation,
                blockDTO: BlockDTO,
                userId: string
            }
        ) => Promise<void>;

}




/**
 * 공통 응답 형식
 */
export interface SocketResponse<T = any> {
    success: boolean;
    data?: T;
    error?: SocketErrorPayload;
    timestamp: string;
    socketId?: string; // 소켓 ID (선택적)
    requestId?: string;
}


/**
 * 공통에러 형식
 */
export interface SocketErrorPayload
{
    code: string;         // 에러 코드 (예: 'AUTH_FAIL', 'NOT_FOUND')
    message: string;      // 사용자/개발자용 메시지
    details?: any;        // (선택) 추가 정보, 디버깅용
}

