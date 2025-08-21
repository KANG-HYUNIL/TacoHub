/**
 * Audit log 정보 추출 유틸리티
 */
import { AuditLog } from "./auditLog.types";

/**
 * 이벤트/메서드 정보 추출
 * @param eventName - WebSocket 이벤트명 (예: 'connect', 'message')
 * @param methodName - 실제 처리 메서드명 (예: 'handleConnect')
 * @returns { eventName: string; methodName?: string }
 */
export function extractEventInfo(eventName: string, methodName?: string): { eventName: string; methodName?: string } {
    return { eventName, methodName };
}

/**
 * 사용자 정보 추출
 * @param context - this 또는 핸들러 컨텍스트
 * @returns { userId?: string; sessionId?: string }
 * userId는 JWT 토큰에서 추출 예정, sessionId는 세션 식별자
 */
export function extractUserInfo(context: any): { userId?: string; sessionId?: string } {
    // TODO: JWT 토큰에서 userId 추출
    return {
        userId: undefined, // 추후 구현
        sessionId: context?.sessionId
    };
}

/**
 * 네트워크 정보(IP) 추출
 * @param context - this 또는 핸들러 컨텍스트
 * @returns { clientIp?: string }
 * 프록시/중간 서버 환경 고려, X-Forwarded-For 등에서 원본 IP 추출
 */
export function extractNetworkInfo(context: any): { clientIp?: string } {
    let ip = context?.ip;
    if (context?.headers) {
        ip = context.headers["x-forwarded-for"] || context.headers["x-real-ip"] || context.ip;
        if (ip && typeof ip === "string" && ip.includes(",")) {
            ip = ip.split(",")[0].trim();
        }
    }
    return { clientIp: ip };
}

/**
 * 파라미터 정보 추출
 * @param args - 메서드 인자 배열
 * @returns { parameters?: any }
 */
export function extractParameters(args: any[]): { parameters?: any } {
    return { parameters: args };
}

/**
 * 결과 정보 추출
 * @param result - 메서드 실행 결과
 * @returns { result?: any }
 */
export function extractResult(result: any): { result?: any } {
    return { result };
}

/**
 * 에러 정보 추출
 * @param error - 발생한 에러 객체
 * @returns { errorType?: string; errorMessage?: string; stackTrace?: string }
 */
export function extractErrorInfo(error: any): { errorType?: string; errorMessage?: string; stackTrace?: string } {
    if (!error) return {};
    return {
        errorType: error?.name || error?.constructor?.name,
        errorMessage: error?.message,
        stackTrace: error?.stack
    };
}

/**
 * 성능(처리 시간) 정보 추출
 * @param start - 시작 시각(ms)
 * @param end - 종료 시각(ms)
 * @returns { durationMs: number }
 */
export function extractPerformance(start: number, end: number): { durationMs: number } {
    return { durationMs: end - start };
}

/**
 * 타임스탬프 정보 추출
 * @returns { timestamp: string }
 */
export function extractTimestamp(): { timestamp: string } {
    return { timestamp: new Date().toISOString() };
}
