export const ERROR_MESSAGES = {
    // 인증 관련
    INVALID_TOKEN: 'Invalid or expired token',
    UNAUTHORIZED: 'Unauthorized access',
    AUTHENTICATION_REQUIRED: 'Authentication required',
    
    // 워크스페이스 관련
    WORKSPACE_NOT_FOUND: 'Workspace not found',
    WORKSPACE_ACCESS_DENIED: 'Access denied to workspace',
    INVALID_WORKSPACE_ID: 'Invalid workspace ID',
    
    // 페이지 관련
    PAGE_NOT_FOUND: 'Page not found',
    PAGE_ACCESS_DENIED: 'Access denied to page',
    INVALID_PAGE_ID: 'Invalid page ID',
    
    // 일반 오류
    INTERNAL_SERVER_ERROR: 'Internal server error',
    VALIDATION_ERROR: 'Validation error',
    NETWORK_ERROR: 'Network connection error',
    
    // Socket 관련
    SOCKET_CONNECTION_FAILED: 'Failed to establish socket connection',
    SOCKET_DISCONNECTED: 'Socket connection disconnected',
    INVALID_SOCKET_EVENT: 'Invalid socket event'
} as const;
