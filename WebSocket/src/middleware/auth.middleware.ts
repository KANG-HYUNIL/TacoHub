import { Socket } from 'socket.io';
import { authService } from '../services/auth.service';
import { logger } from '../utils/logger';

export interface AuthenticatedSocket extends Socket {
    userId?: string;
    userEmail?: string;
}

export function authMiddleware(socket: AuthenticatedSocket, next: (err?: Error) => void) {
    try {
        const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace('Bearer ', '');
        
        if (!token) {
            logger.warn(`Authentication failed: No token provided for socket ${socket.id}`);
            return next(new Error('Authentication error: Token required'));
        }
        
        const payload = authService.verifyToken(token);
        if (!payload) {
            logger.warn(`Authentication failed: Invalid token for socket ${socket.id}`);
            return next(new Error('Authentication error: Invalid token'));
        }
        
        // Socket에 사용자 정보 추가
        socket.userId = payload.userId;
        socket.userEmail = payload.email;
        
        logger.info(`Socket ${socket.id} authenticated for user ${payload.userId}`);
        next();
    } catch (error) {
        logger.error(`Authentication middleware error for socket ${socket.id}:`, error);
        next(new Error('Authentication error'));
    }
}
