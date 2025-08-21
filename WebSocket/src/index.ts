/**
 * @fileoverview WebSocket 서버의 메인 진입점
 * 
 * 이 파일은 TacoHub WebSocket 서버의 시작점으로, 다음과 같은 역할을 담당합니다:
 * 1. 환경변수 로드 및 초기화
 * 2. HTTP/Socket.IO 서버 생성 및 설정
 * 3. 서버 시작 및 에러 핸들링
 * 4. 프로세스 수준의 예외 처리
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import dotenv from 'dotenv';
import { createServer } from './server';
import { applicationLogger } from './utils/logger';

// 환경변수 로드 - .env 파일의 설정값들을 process.env에 주입
dotenv.config();

// 서버 포트 설정 - 환경변수에서 포트를 가져오거나 기본값 3001 사용
const PORT = process.env.PORT || 3001;

/**
 * 서버 시작 함수
 * Express + Socket.IO 서버를 생성하고 지정된 포트에서 리스닝을 시작합니다.
 * 
 * @async
 * @function startServer
 * @throws {Error} 서버 시작 실패 시 프로세스 종료
 */
async function startServer() {
    try {
        // HTTP 서버 인스턴스 생성 (Express + Socket.IO 포함)
        const server = await createServer();
        
        // 지정된 포트에서 서버 시작
        server.listen(PORT, () => {
            applicationLogger.info(`WebSocket Server running on port ${PORT}`);
        });
    } catch (error) {
        applicationLogger.error('Failed to start server:', error);
        process.exit(1); // 서버 시작 실패 시 프로세스 종료
    }
}

// 서버 시작 실행
startServer();
