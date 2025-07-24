/**
 * @fileoverview Socket.IO 이벤트 타입 정의
 * 
 * 이 파일은 TacoHub WebSocket 서버에서 사용되는 모든 Socket.IO 이벤트의 타입을 정의합니다:
 * 1. 클라이언트-서버 간 이벤트 타입 안전성 보장
 * 2. TypeScript 자동완성 및 컴파일 타임 오류 검출
 * 3. API 문서화 및 유지보수성 향상
 * 4. Frontend와 Backend 간 이벤트 명세 통일
 * 
 * 모든 이벤트는 명명 규칙을 따르며, 도메인별로 네임스페이스가 분리되어 있습니다.
 * (connection, workspace, page, cursor, notification 등)
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

/**
 * Socket.IO 이벤트 맵 인터페이스
 * 
 * 모든 WebSocket 이벤트와 그에 대응하는 데이터 타입을 정의합니다.
 * 이벤트 명명 규칙: {domain}:{action} (예: workspace:join, page:edit)
 * 
 * @interface SocketEventMap
 */
export interface SocketEventMap {
    
    // ====================================================================
    // 🔌 연결 관련 이벤트 (Connection Events)
    // ====================================================================
    
    /**
     * 클라이언트 연결 이벤트
     * Socket.IO 내장 이벤트로, 새로운 클라이언트가 연결될 때 발생
     */
    'connection': () => void;
    
    /**
     * 클라이언트 연결 해제 이벤트
     * Socket.IO 내장 이벤트로, 클라이언트가 연결을 해제할 때 발생
     */
    'disconnect': () => void;
    
    /**
     * 서버 → 클라이언트: 연결 성공 알림
     * 인증이 완료되고 WebSocket 연결이 성공적으로 수립되었음을 알림
     */
    'connected': (data: { 
        message: string;      // 연결 성공 메시지
        socketId: string;     // 소켓 고유 ID
        timestamp: string;    // 연결 시간
        serverVersion: string; // 서버 버전
    }) => void;
    
    // ====================================================================
    // 🏢 워크스페이스 관련 이벤트 (Workspace Events)
    // ====================================================================
    
    /**
     * 클라이언트 → 서버: 워크스페이스 입장 요청
     * 사용자가 특정 워크스페이스에 입장하고자 할 때 전송
     */
    'workspace:join': (data: { 
        workspaceId: string;  // 입장할 워크스페이스 ID
        userId: string;       // 입장하는 사용자 ID
    }) => void;
    
    /**
     * 클라이언트 → 서버: 워크스페이스 퇴장 요청
     * 사용자가 워크스페이스를 떠날 때 전송
     */
    'workspace:leave': (data: { 
        workspaceId: string;  // 퇴장할 워크스페이스 ID
        userId: string;       // 퇴장하는 사용자 ID
    }) => void;
    
    /**
     * 서버 → 클라이언트: 새 사용자 입장 알림
     * 워크스페이스에 새로운 멤버가 입장했음을 기존 멤버들에게 알림
     */
    'user:joined': (data: { 
        userId: string;       // 입장한 사용자 ID
        username: string;     // 사용자 이름
        workspaceId: string;  // 워크스페이스 ID
        timestamp: string;    // 입장 시간
        socketId: string;     // 소켓 ID
    }) => void;
    
    /**
     * 서버 → 클라이언트: 사용자 퇴장 알림
     * 워크스페이스에서 멤버가 퇴장했음을 다른 멤버들에게 알림
     */
    'user:left': (data: { 
        userId: string;       // 퇴장한 사용자 ID
        username: string;     // 사용자 이름
        workspaceId: string;  // 워크스페이스 ID
        timestamp: string;    // 퇴장 시간
        socketId: string;     // 소켓 ID
    }) => void;
    
    // ====================================================================
    // 📝 페이지 편집 관련 이벤트 (Page Editing Events)
    // ====================================================================
    
    /**
     * 클라이언트 → 서버: 페이지 편집 세션 입장
     * 특정 페이지의 실시간 편집을 시작할 때 전송
     */
    'page:join': (data: { 
        pageId: string;       // 편집할 페이지 ID
        workspaceId: string;  // 페이지가 속한 워크스페이스 ID
        userId: string;       // 편집하는 사용자 ID
    }) => void;
    
    /**
     * 클라이언트 → 서버: 페이지 편집 세션 퇴장
     * 페이지 편집을 종료할 때 전송
     */
    'page:leave': (data: { 
        pageId: string;       // 편집 종료할 페이지 ID
        workspaceId: string;  // 워크스페이스 ID
        userId: string;       // 사용자 ID
    }) => void;
    
    /**
     * 클라이언트 → 서버: 페이지 내용 편집
     * 실제 페이지 내용을 편집할 때 전송하는 메인 이벤트
     */
    'page:edit': (data: { 
        pageId: string;       // 편집하는 페이지 ID
        workspaceId: string;  // 워크스페이스 ID
        userId: string;       // 편집하는 사용자 ID
        content: any;         // 편집된 내용 (텍스트, HTML, JSON 등)
        operation: string;    // 편집 작업 타입 ('insert', 'delete', 'replace' 등)
        position?: number;    // 편집 위치 (문자 인덱스)
        blockId?: string;     // 편집된 블록 ID
        version?: number;     // 문서 버전 (충돌 감지용)
    }) => void;
    
    /**
     * 서버 → 클라이언트: 페이지 업데이트 알림
     * 다른 사용자의 편집으로 인한 페이지 변경사항을 실시간으로 알림
     */
    'page:updated': (data: { 
        pageId: string;       // 업데이트된 페이지 ID
        userId: string;       // 편집한 사용자 ID
        username: string;     // 편집한 사용자 이름
        content: any;         // 변경된 내용
        operation: string;    // 수행된 편집 작업
        timestamp: string;    // 편집 시간
        version: number;      // 업데이트된 문서 버전
        blockId?: string;     // 편집된 블록 ID
    }) => void;
    
    // ====================================================================
    // 👆 커서 및 선택 영역 관련 이벤트 (Cursor Events)
    // ====================================================================
    
    /**
     * 클라이언트 → 서버: 커서 위치 업데이트
     * 사용자가 커서를 이동하거나 텍스트를 선택할 때 전송
     */
    'cursor:update': (data: { 
        pageId: string;       // 커서가 위치한 페이지 ID
        userId: string;       // 사용자 ID
        userName: string;     // 사용자 이름
        position: {
            x: number;        // 화면 X 좌표
            y: number;        // 화면 Y 좌표
            textPosition?: number;  // 텍스트 내 위치 (문자 인덱스)
            blockId?: string; // 커서가 위치한 블록 ID
            lineNumber?: number;    // 라인 번호
            columnNumber?: number;  // 컬럼 번호
        };
        selection?: {         // 텍스트 선택 영역 (있는 경우)
            start: number;    // 선택 시작 위치
            end: number;      // 선택 끝 위치
            text: string;     // 선택된 텍스트
        };
    }) => void;
    
    /**
     * 서버 → 클라이언트: 커서 위치 변경 알림
     * 다른 사용자의 커서 위치 변경을 실시간으로 동기화
     */
    'cursor:moved': (data: { 
        pageId: string;       // 페이지 ID
        userId: string;       // 커서를 이동한 사용자 ID
        userName: string;     // 사용자 이름
        position: {
            x: number;        // 화면 X 좌표
            y: number;        // 화면 Y 좌표
            textPosition?: number;  // 텍스트 위치
            blockId?: string; // 블록 ID
        };
        selection?: {         // 선택 영역
            start: number;
            end: number;
            text: string;
        };
        timestamp: string;    // 이동 시간
        color?: string;       // 커서 색상 (사용자별 구분)
    }) => void;
    
    /**
     * 클라이언트 → 서버: 커서 숨김 요청
     * 사용자가 페이지를 떠나거나 비활성화될 때 커서를 숨김
     */
    'cursor:hide': (data: { 
        pageId: string;       // 페이지 ID
        userId: string;       // 사용자 ID
    }) => void;
    
    /**
     * 서버 → 클라이언트: 커서 숨김 알림
     * 특정 사용자의 커서가 숨겨졌음을 다른 사용자들에게 알림
     */
    'cursor:hidden': (data: { 
        pageId: string;       // 페이지 ID
        userId: string;       // 커서를 숨긴 사용자 ID
        timestamp: string;    // 숨김 시간
    }) => void;
    
    // ====================================================================
    // 🔔 알림 관련 이벤트 (Notification Events)
    // ====================================================================
    
    /**
     * 클라이언트 → 서버: 알림 전송 요청
     * 사용자가 다른 사용자나 워크스페이스에 알림을 보낼 때 사용
     */
    'notification:send': (data: { 
        workspaceId: string;  // 대상 워크스페이스 ID
        userId: string;       // 알림을 보내는 사용자 ID
        type: string;         // 알림 타입 ('mention', 'invite', 'comment' 등)
        message: string;      // 알림 메시지
        targetUserIds?: string[];  // 특정 사용자 대상 (선택적)
        data?: any;          // 추가 데이터 (페이지 링크, 첨부파일 등)
        priority?: 'high' | 'normal' | 'low';  // 알림 우선순위
    }) => void;
    
    /**
     * 서버 → 클라이언트: 알림 수신
     * 사용자가 받은 알림을 실시간으로 전달
     */
    'notification:received': (data: { 
        id: string;           // 알림 고유 ID
        type: string;         // 알림 타입
        message: string;      // 알림 메시지
        fromUserId: string;   // 알림을 보낸 사용자 ID
        fromUserName: string; // 알림을 보낸 사용자 이름
        workspaceId?: string; // 관련 워크스페이스 ID
        pageId?: string;      // 관련 페이지 ID
        data?: any;          // 추가 데이터
        timestamp: string;    // 알림 시간
        priority: string;     // 우선순위
        read: boolean;        // 읽음 상태
    }) => void;
}

/**
 * Socket.IO 서버 이벤트 타입 (서버 내부용)
 * 서버에서만 사용되는 내부 이벤트들을 정의
 */
export interface ServerEventMap {
    'server:start': () => void;
    'server:stop': () => void;
    'server:error': (error: Error) => void;
    'server:metrics': (data: { connections: number; rooms: number; memory: number }) => void;
}

/**
 * 이벤트 데이터 검증을 위한 유틸리티 타입들
 */
export type EventName = keyof SocketEventMap;
export type EventData<T extends EventName> = Parameters<SocketEventMap[T]>[0];

/**
 * 이벤트 핸들러 타입 정의
 */
export type EventHandler<T extends EventName> = SocketEventMap[T];

/**
 * 공통 응답 형식
 */
export interface SocketResponse<T = any> {
    success: boolean;
    data?: T;
    error?: {
        code: string;
        message: string;
        details?: any;
    };
    timestamp: string;
    requestId?: string;
}
