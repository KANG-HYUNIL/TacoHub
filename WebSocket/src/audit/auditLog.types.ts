/**
 * WebSocket Audit Log 타입 정의
 */
export interface AuditLog {
    eventName: string;
    methodName?: string;
    userId?: string; // TODO: JWT 토큰에서 추출 예정
    sessionId?: string;
    clientIp?: string;
    parameters?: any;
    payload?: any;
    result?: any;
    durationMs?: number;
    errorType?: string;
    errorMessage?: string;
    stackTrace?: string;
    timestamp: string;
}
