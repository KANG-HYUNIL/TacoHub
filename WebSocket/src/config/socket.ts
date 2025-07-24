/**
 * @fileoverview Socket.IO 서버 설정 및 연결 관리
 * 
 * 이 파일은 Socket.IO 서버의 핵심 설정을 담당하며, 클라이언트 연결을 관리합니다:
 * 1. Socket.IO 서버 이벤트 핸들러 등록
 * 2. 클라이언트 연결/해제 처리
 * 3. 인증 미들웨어 적용
 * 4. 각종 네임스페이스별 이벤트 핸들러 연결
 * 
 * 실시간 협업을 위한 WebSocket 통신의 중앙 허브 역할을 수행합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Server as SocketIOServer } from 'socket.io';
import { logger } from '../utils/logger';
import { setupConnectionHandler } from '../handlers/connection.handler';

/**
 * Socket.IO 서버 설정 및 이벤트 핸들러 등록
 * 
 * @function setupSocketIO
 * @param {SocketIOServer} io - Socket.IO 서버 인스턴스
 * 
 * 설정 내용:
 * - 연결 핸들러 설정
 * - 미들웨어 등록 (향후 확장 예정)
 * - 네임스페이스 및 룸 관리 설정
 * - 로깅 시스템 연동
 */
export function setupSocketIO(io: SocketIOServer) {
    logger.info('Setting up Socket.IO...');
    
    /**
     * 연결 핸들러 설정
     * 클라이언트 연결/해제 및 기본 이벤트 처리를 담당하는 핸들러를 등록합니다.
     * 인증, 워크스페이스 입장, 페이지 편집 등의 모든 이벤트 처리가 이곳에서 시작됩니다.
     */
    setupConnectionHandler(io);
    
    logger.info('Socket.IO setup completed');
}
