// WebSocket 이벤트 상수 정의

export const SOCKET_EVENTS = {
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
    NOTIFICATION_RECEIVED: 'notification:received'
} as const;
