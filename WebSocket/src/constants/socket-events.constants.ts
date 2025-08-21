/**
 * @fileoverview 실시간 소켓 이벤트명 및 유틸 상수 정의
 *
 * - 소켓 이벤트명을 문자열이 아닌 상수로 관리
 * - roomName 생성 유틸 함수 제공
 *
 * @author TacoHub Team
 * @version 1.0.0
 */


/**
 * 워크스페이스/페이지 기반 roomName 생성 유틸
 * @param workspaceId 워크스페이스 ID
 * @param pageId 페이지 ID
 * @returns roomName (예: workspaceId:pageId)
 */
export function getRoomName(workspaceId: string, pageId: string): string {
  return `${workspaceId}:${pageId}`;
}// WebSocket 이벤트 상수 정의

export const SOCKET_EVENTS = {

    // jwt 관련
    AUTHENTICATION_REQUIRED: 'authentication_required', // 인증 필요

    // 블럭 관련
    BLOCK_UPDATE: 'block_update', // 클라이언트 → 서버 (block 수정 요청)
    BLOCK_UPDATE_BROADCAST: 'block:update', // 서버 → 클라이언트 (block 수정 전파)

    // 연결 관련
    CONNECTION: 'connection',
    DISCONNECT: 'disconnect',
    CONNECTED: 'connected',
    
    // 워크스페이스 관련
    WORKSPACE_JOIN: 'workspace:join',
    WORKSPACE_LEAVE: 'workspace:leave',
    USER_JOINED: 'user:joined',
    USER_LEFT: 'user:left',
    
    // 페이지 편집 관련
    PAGE_JOIN: 'page:join',
    PAGE_LEAVE: 'page:leave',
    PAGE_EDIT: 'page:edit',
    PAGE_UPDATED: 'page:updated',
    
    // 커서 관련
    CURSOR_UPDATE: 'cursor:update',
    CURSOR_MOVED: 'cursor:moved',
    CURSOR_HIDE: 'cursor:hide',
    CURSOR_HIDDEN: 'cursor:hidden',
    
    // 알림 관련
    NOTIFICATION_SEND: 'notification:send',
    NOTIFICATION_RECEIVED: 'notification:received',

    // 에러
    ERROR : 'error',
} as const;
