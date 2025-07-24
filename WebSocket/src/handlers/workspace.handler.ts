/**
 * @fileoverview 워크스페이스 관련 WebSocket 이벤트 핸들러
 * 
 * 이 파일은 워크스페이스와 관련된 실시간 이벤트를 처리합니다:
 * 1. 워크스페이스 입장/퇴장 관리
 * 2. 워크스페이스 멤버 상태 동기화
 * 3. 워크스페이스 권한 검증 및 관리
 * 4. 실시간 워크스페이스 정보 업데이트
 * 
 * TacoHub의 협업 환경에서 사용자들이 워크스페이스를 공유하고
 * 실시간으로 상호작용할 수 있는 기능을 제공합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Socket } from 'socket.io';
import { logger } from '../utils/logger';

/**
 * 워크스페이스 이벤트 데이터 인터페이스
 * 워크스페이스 관련 이벤트에서 사용되는 데이터 구조를 정의합니다
 * 
 * @interface WorkspaceEventData
 * @property {string} workspaceId - 워크스페이스 고유 식별자
 * @property {string} userId - 사용자 고유 식별자
 * @property {string} action - 수행할 액션 타입
 * @property {any} [data] - 추가 데이터 (선택적)
 */
export interface WorkspaceEventData {
    workspaceId: string;
    userId: string;
    action: string;
    data?: any;
}

/**
 * 워크스페이스 관련 이벤트 핸들러 설정
 * 
 * @function setupWorkspaceHandler
 * @param {Socket} socket - 클라이언트 Socket 인스턴스
 * 
 * 등록되는 이벤트:
 * - workspace:join - 워크스페이스 입장
 * - workspace:leave - 워크스페이스 퇴장
 * 
 * Socket.IO의 Room 기능을 활용하여 워크스페이스별로 사용자를 그룹화하고
 * 실시간 알림을 전송합니다.
 */
export function setupWorkspaceHandler(socket: Socket) {
    
    /**
     * 워크스페이스 입장 이벤트 처리
     * 사용자가 특정 워크스페이스에 입장할 때 실행됩니다
     * 
     * @event workspace:join
     * @param {WorkspaceEventData} data - 워크스페이스 입장 데이터
     * 
     * 처리 과정:
     * 1. 사용자를 워크스페이스 룸에 추가
     * 2. 다른 워크스페이스 멤버들에게 새 사용자 입장 알림
     * 3. 로깅 및 상태 추적
     */
    socket.on('workspace:join', (data: WorkspaceEventData) => {
        logger.info(`User ${data.userId} joining workspace ${data.workspaceId}`);
        
        // Socket.IO Room 기능을 사용하여 워크스페이스 그룹에 참가
        socket.join(`workspace:${data.workspaceId}`);
        
        /**
         * 워크스페이스의 다른 사용자들에게 새 멤버 입장 알림
         * socket.to()를 사용하여 현재 소켓을 제외한 같은 룸의 모든 소켓에게 전송
         */
        socket.to(`workspace:${data.workspaceId}`).emit('user:joined', {
            userId: data.userId,
            workspaceId: data.workspaceId,
            timestamp: new Date().toISOString(),
            socketId: socket.id
        });
        
        // TODO: 향후 확장 시 추가할 기능들
        // - Spring Boot API를 통한 워크스페이스 접근 권한 검증
        // - 워크스페이스 메타데이터 전송 (멤버 목록, 페이지 목록 등)
        // - 사용자의 마지막 활동 상태 업데이트
    });
    
    /**
     * 워크스페이스 퇴장 이벤트 처리
     * 사용자가 워크스페이스를 떠날 때 실행됩니다
     * 
     * @event workspace:leave
     * @param {WorkspaceEventData} data - 워크스페이스 퇴장 데이터
     * 
     * 처리 과정:
     * 1. 사용자를 워크스페이스 룸에서 제거
     * 2. 다른 워크스페이스 멤버들에게 사용자 퇴장 알림
     * 3. 로깅 및 상태 정리
     */
    socket.on('workspace:leave', (data: WorkspaceEventData) => {
        logger.info(`User ${data.userId} leaving workspace ${data.workspaceId}`);
        
        // Socket.IO Room에서 사용자 제거
        socket.leave(`workspace:${data.workspaceId}`);
        
        /**
         * 워크스페이스의 다른 사용자들에게 멤버 퇴장 알림
         * 연결이 끊어진 후에도 다른 사용자들이 상태를 알 수 있도록 함
         */
        socket.to(`workspace:${data.workspaceId}`).emit('user:left', {
            userId: data.userId,
            workspaceId: data.workspaceId,
            timestamp: new Date().toISOString(),
            socketId: socket.id
        });
        
        // TODO: 향후 확장 시 추가할 기능들
        // - 사용자가 편집 중이던 페이지의 락 해제
        // - 임시 데이터 정리 (커서 위치, 선택 영역 등)
        // - 마지막 활동 시간 기록
    });
}
