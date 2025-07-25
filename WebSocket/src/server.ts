/**
 * @fileoverview HTTP 서버 및 Socket.IO 통합 설정
 * 
 * 이 파일은 Express HTTP 서버와 Socket.IO WebSocket 서버를 통합하여 설정하는 역할을 담당합니다:
 * 1. Express 애플리케이션 생성 및 미들웨어 설정
 * 2. HTTP 서버 생성 및 Socket.IO 서버 연결
 * 3. CORS, 보안, 로깅 등의 미들웨어 구성
 * 4. 기본 REST API 엔드포인트 제공 (헬스체크 등)
 * 
 * Socket.IO와 Express를 함께 사용하여 REST API와 실시간 통신을 모두 지원합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import express from 'express';
import { createServer as createHttpServer } from 'http';
import { Server as SocketIOServer } from 'socket.io';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';

import { setupSocketIO } from './config/socket';
import { applicationLogger } from './utils/logger';
import { handleError } from './utils/error-handler';
import { ServerError } from './types/error/ServerError';
import { startRabbitMQ } from './rabbitmq/rabbitmq.config';

/**
 * HTTP 서버와 Socket.IO 서버를 생성하고 설정하는 함수
 * 
 * @async
 * @function createServer
 * @returns {Promise<http.Server>} 설정이 완료된 HTTP 서버 인스턴스
 * 
 * 주요 설정 내용:
 * - Express 미들웨어 (CORS, Helmet, Morgan, JSON parser)
 * - Socket.IO 서버 생성 및 이벤트 핸들러 연결
 * - REST API 엔드포인트 설정
 */
export async function createServer() {
    try {
        // Express 애플리케이션 인스턴스 생성
        const app = express();

        // HTTP 서버 생성 (Socket.IO가 이를 기반으로 동작)
        const httpServer = createHttpServer(app);

        // === 보안 및 기본 미들웨어 설정 ===
        app.use(helmet()); // 보안 헤더 설정 (XSS, CSRF 등 방어)
        app.use(cors());   // Cross-Origin 요청 허용
        app.use(morgan('combined')); // HTTP 요청 로깅
        app.use(express.json()); // JSON 요청 본문 파싱

        // === RabbitMQ 연결 및 초기화 ===
        await startRabbitMQ();

        // === Socket.IO 서버 설정 ===
        const io = new SocketIOServer(httpServer, {
            cors: {
                // 프론트엔드 도메인에서의 요청 허용
                origin: process.env.FRONTEND_URL || "http://localhost:3000",
                methods: ["GET", "POST"]
            }
        });

        // Socket.IO 이벤트 핸들러 및 미들웨어 설정
        setupSocketIO(io);

        // === REST API 엔드포인트 ===
        app.get('/health', (req, res) => {
            res.json({ 
                status: 'ok', 
                message: 'WebSocket Server is running',
                timestamp: new Date().toISOString()
            });
        });

        return httpServer;
    } catch (error) {
        handleError(new ServerError(`[createServer] 서버 기동 중 오류 발생: ${error instanceof Error ? error.message : String(error)}`), 'createServer');
    }
}
