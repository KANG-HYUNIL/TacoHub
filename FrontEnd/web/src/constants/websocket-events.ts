// WebSocket 이벤트 상수 및 타입 예시
export const SOCKET_EVENTS = {
  AUTHENTICATION_REQUIRED: 'authentication_required',
  BLOCK_UPDATE: 'block_update',
  BLOCK_UPDATE_BROADCAST: 'block:update',
  CONNECTION: 'connection',
  DISCONNECT: 'disconnect',
  CONNECTED: 'connected',
  WORKSPACE_JOIN: 'workspace:join',
  WORKSPACE_LEAVE: 'workspace:leave',
  USER_JOINED: 'user:joined',
  USER_LEFT: 'user:left',
  PAGE_JOIN: 'page:join',
  PAGE_LEAVE: 'page:leave',
  PAGE_EDIT: 'page:edit',
  PAGE_UPDATED: 'page:updated',
  CURSOR_UPDATE: 'cursor:update',
  CURSOR_MOVED: 'cursor:moved',
  CURSOR_HIDE: 'cursor:hide',
  CURSOR_HIDDEN: 'cursor:hidden',
  NOTIFICATION_SEND: 'notification:send',
  NOTIFICATION_RECEIVED: 'notification:received',
  ERROR: 'error',
} as const;


