import { logger } from './logger';

export class CustomError extends Error {
    public statusCode: number;
    
    constructor(message: string, statusCode: number = 500) {
        super(message);
        this.statusCode = statusCode;
        this.name = this.constructor.name;
    }
}

export function handleSocketError(error: Error, context: string): void {
    logger.error(`Socket error in ${context}:`, {
        message: error.message,
        stack: error.stack,
        name: error.name
    });
}

export function handleServiceError(error: Error, serviceName: string, method: string): never {
    logger.error(`Service error in ${serviceName}.${method}:`, {
        message: error.message,
        stack: error.stack,
        name: error.name
    });
    
    throw new CustomError(`${serviceName} service error: ${error.message}`, 500);
}
