import { Socket } from 'socket.io';
import { validateUUID, validateRequired, ValidationError } from '../utils/validator';
import { logger } from '../utils/logger';

export function validationMiddleware(eventName: string, data: any, socket: Socket, next: (err?: Error) => void) {
    try {
        switch (eventName) {
            case 'workspace:join':
            case 'workspace:leave':
                validateRequired(data.workspaceId, 'workspaceId');
                validateRequired(data.userId, 'userId');
                if (!validateUUID(data.workspaceId)) {
                    throw new ValidationError('Invalid workspace ID format');
                }
                break;
                
            case 'page:join':
            case 'page:leave':
            case 'page:edit':
                validateRequired(data.pageId, 'pageId');
                validateRequired(data.workspaceId, 'workspaceId');
                validateRequired(data.userId, 'userId');
                if (!validateUUID(data.pageId)) {
                    throw new ValidationError('Invalid page ID format');
                }
                if (!validateUUID(data.workspaceId)) {
                    throw new ValidationError('Invalid workspace ID format');
                }
                break;
                
            case 'cursor:update':
                validateRequired(data.pageId, 'pageId');
                validateRequired(data.userId, 'userId');
                validateRequired(data.position, 'position');
                if (!validateUUID(data.pageId)) {
                    throw new ValidationError('Invalid page ID format');
                }
                break;
        }
        
        next();
    } catch (error) {
        if (error instanceof ValidationError) {
            logger.warn(`Validation error for event ${eventName} on socket ${socket.id}:`, error.message);
            next(error);
        } else {
            logger.error(`Validation middleware error for event ${eventName} on socket ${socket.id}:`, error);
            next(new Error('Validation error'));
        }
    }
}
