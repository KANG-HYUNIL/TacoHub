/**
 * @fileoverview 커서 및 선택 영역 실시간 동기화 핸들러
 * 
 * 이 파일은 다중 사용자 환경에서 커서 위치와 선택 영역을 실시간으로 동기화합니다:
 * 1. 사용자별 커서 위치 실시간 추적 및 표시
 * 2. 텍스트 선택 영역 공유 및 동기화
 * 3. 커서 표시/숨김 관리 (사용자 입장/퇴장 시)
 * 4. 블록별 편집 상태 시각화
 * 
 * 협업 편집 환경에서 사용자들이 서로의 편집 위치를 실시간으로 확인할 수 있도록
 * 하여 편집 충돌을 방지하고 원활한 협업을 지원합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import { Socket } from 'socket.io';
import { logger } from '../utils/logger';

/**
 * 커서 이벤트 데이터 인터페이스
 * 커서 위치 및 선택 영역 정보를 정의합니다
 * 
 * @interface CursorEventData
 * @property {string} pageId - 페이지 고유 식별자
 * @property {string} userId - 사용자 고유 식별자
 * @property {string} userName - 사용자 표시 이름
 * @property {Object} position - 커서 위치 정보
 * @property {number} position.x - X 좌표 (픽셀 단위)
 * @property {number} position.y - Y 좌표 (픽셀 단위)
 * @property {string} [position.blockId] - 커서가 위치한 블록 ID (선택적)
 */
export interface CursorEventData {
    pageId: string;
    userId: string;
    userName: string;
    position: {
        x: number;
        y: number;
        blockId?: string;
    };
}

/**
 * 커서 관련 이벤트 핸들러 설정
 * 
 * @function setupCursorHandler
 * @param {Socket} socket - 클라이언트 Socket 인스턴스
 * 
 * 등록되는 이벤트:
 * - cursor:update - 커서 위치 업데이트
 * - cursor:hide - 커서 숨김 (사용자 퇴장 시)
 * 
 * 실시간 커서 동기화를 통해 다중 사용자 편집 환경에서
 * 각 사용자의 위치를 시각적으로 표시합니다.
 */
export function setupCursorHandler(socket: Socket) {
    
    /**
     * 커서 위치 업데이트 이벤트 처리
     * 사용자가 커서를 이동할 때 실행됩니다
     * 
     * @event cursor:update
     * @param {CursorEventData} data - 커서 위치 데이터
     * 
     * 처리 과정:
     * 1. 커서 위치 정보 검증 및 로깅
     * 2. 같은 페이지를 보고 있는 다른 사용자들에게 커서 위치 실시간 전파
     * 3. 블록 단위 편집 상태 표시 (향후 확장)
     */
    socket.on('cursor:update', (data: CursorEventData) => {
        /**
         * 같은 페이지의 다른 사용자들에게 커서 위치 브로드캐스트
         * 페이지별 룸을 사용하여 관련 없는 사용자에게는 전송하지 않음
         */
        socket.to(`page:${data.pageId}`).emit('cursor:moved', {
            userId: data.userId,
            userName: data.userName,
            position: data.position,
            timestamp: new Date().toISOString(),
            socketId: socket.id // 중복 커서 방지를 위한 소켓 ID
        });
        
        // 디버깅을 위한 상세 로깅 (개발 환경에서만)
        if (process.env.NODE_ENV === 'development') {
            logger.debug(`Cursor updated: User ${data.userName} at (${data.position.x}, ${data.position.y}) in page ${data.pageId}`);
        }
        
        // TODO: 향후 확장 시 추가할 기능들
        // - 커서 위치 기반 블록 하이라이팅
        // - 편집 충돌 감지 (같은 블록에 여러 사용자가 있을 때)
        // - 커서 위치 히스토리 추적 (분석용)
        // - 사용자별 커서 색상 및 스타일 관리
    });
    
    /**
     * 커서 숨김 이벤트 처리
     * 사용자가 페이지를 떠나거나 비활성화될 때 실행됩니다
     * 
     * @event cursor:hide
     * @param {Object} data - 커서 숨김 데이터
     * @param {string} data.pageId - 페이지 ID
     * @param {string} data.userId - 사용자 ID
     * 
     * 처리 과정:
     * 1. 다른 사용자들에게 해당 사용자의 커서 제거 알림
     * 2. UI에서 커서 표시 정리
     * 3. 관련 상태 데이터 정리
     */
    socket.on('cursor:hide', (data: { pageId: string; userId: string }) => {
        /**
         * 같은 페이지의 다른 사용자들에게 커서 숨김 알림
         * 클라이언트에서 해당 사용자의 커서를 UI에서 제거하도록 함
         */
        socket.to(`page:${data.pageId}`).emit('cursor:hidden', {
            userId: data.userId,
            timestamp: new Date().toISOString()
        });
        
        logger.info(`Cursor hidden for user ${data.userId} in page ${data.pageId}`);
        
        // TODO: 향후 확장 시 추가할 기능들
        // - 해당 사용자의 선택 영역도 함께 제거
        // - 편집 중이던 블록의 락 상태 확인 및 해제
        // - 사용자 상태 데이터 정리
    });
    
    // TODO: 향후 추가할 이벤트들
    // - cursor:select - 텍스트 선택 영역 동기화
    // - cursor:scroll - 스크롤 위치 동기화 (선택적)
    // - cursor:focus - 포커스 상태 동기화
    // - cursor:typing - 타이핑 상태 표시 (typing indicator)
}
