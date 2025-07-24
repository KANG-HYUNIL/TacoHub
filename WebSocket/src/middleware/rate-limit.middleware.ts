import { Socket } from 'socket.io';
import { logger } from '../utils/logger';

interface RateLimitInfo {
    count: number;
    resetTime: number;
}

const rateLimitMap = new Map<string, RateLimitInfo>();

export function rateLimitMiddleware(
    maxRequests: number = 100, 
    windowMs: number = 60000 // 1분
) {
    return (socket: Socket, next: (err?: Error) => void) => {
        const clientId = socket.handshake.address;
        const now = Date.now();
        const windowStart = now - windowMs;
        
        let rateLimitInfo = rateLimitMap.get(clientId);
        
        if (!rateLimitInfo || rateLimitInfo.resetTime < windowStart) {
            // 새 윈도우 시작 또는 첫 요청
            rateLimitInfo = {
                count: 1,
                resetTime: now + windowMs
            };
            rateLimitMap.set(clientId, rateLimitInfo);
            return next();
        }
        
        if (rateLimitInfo.count >= maxRequests) {
            logger.warn(`Rate limit exceeded for client ${clientId}, socket ${socket.id}`);
            return next(new Error('Rate limit exceeded'));
        }
        
        rateLimitInfo.count++;
        next();
    };
}

// 정리 작업을 위한 주기적 실행 (메모리 누수 방지)
setInterval(() => {
    const now = Date.now();
    for (const [clientId, info] of rateLimitMap.entries()) {
        if (info.resetTime < now) {
            rateLimitMap.delete(clientId);
        }
    }
}, 300000); // 5분마다 정리
