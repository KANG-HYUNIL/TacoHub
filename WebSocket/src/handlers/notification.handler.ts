import { Socket } from 'socket.io';
import { logger } from '../utils/logger';

export interface NotificationEventData {
    workspaceId: string;
    userId: string;
    type: 'invitation' | 'mention' | 'comment' | 'update';
    message: string;
    data?: any;
}

export function setupNotificationHandler(socket: Socket) {
    // 알림 전송
    socket.on('notification:send', (data: NotificationEventData) => {
        logger.info(`Sending notification to workspace ${data.workspaceId}`);
        
        // 워크스페이스의 모든 사용자에게 알림 전송
        socket.to(`workspace:${data.workspaceId}`).emit('notification:received', {
            type: data.type,
            message: data.message,
            userId: data.userId,
            data: data.data,
            timestamp: new Date().toISOString()
        });
    });
}
