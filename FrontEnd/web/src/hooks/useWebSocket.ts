import { useEffect, useRef, useState } from 'react';
import { io, Socket } from 'socket.io-client';
import * as Y from 'yjs';

/**
 * WebSocket 연결 상태 타입
 */
export type WebSocketConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

/**
 * WebSocket Hook의 반환 타입
 */
interface UseWebSocketReturn {
  socket: Socket | null;
  connectionStatus: WebSocketConnectionStatus;
  isConnected: boolean;
  connect: () => void;
  disconnect: () => void;
  joinPage: (workspaceId: string, pageId: string) => void;
  leavePage: () => void;
  sendBlockUpdate: (blockData: any) => void;
}

/**
 * WebSocket Hook 파라미터
 */
interface UseWebSocketParams {
  /** WebSocket 서버 URL */
  serverUrl?: string;
  /** JWT 액세스 토큰 */
  accessToken?: string | null;
  /** 자동 연결 여부 */
  autoConnect?: boolean;
  /** 재연결 시도 여부 */
  autoReconnect?: boolean;
}

/**
 * WebSocket 연결을 관리하는 커스텀 Hook
 * 
 * TacoHub WebSocket 서버와의 연결을 관리하고,
 * 페이지 입장/퇴장, 실시간 협업 기능을 제공합니다.
 * 
 * @param params WebSocket 연결 설정
 * @returns WebSocket 연결 정보 및 제어 함수들
 */
export const useWebSocket = ({
  serverUrl = process.env.REACT_APP_WEBSOCKET_URL || 'http://localhost:3001',
  accessToken = null,
  autoConnect = false,
  autoReconnect = true
}: UseWebSocketParams = {}): UseWebSocketReturn => {
  
  // 상태 관리
  const [connectionStatus, setConnectionStatus] = useState<WebSocketConnectionStatus>('disconnected');
  const [socket, setSocket] = useState<Socket | null>(null);
  const [currentRoom, setCurrentRoom] = useState<string | null>(null);
  
  // Ref로 관리할 값들
  const socketRef = useRef<Socket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const isManualDisconnectRef = useRef(false);

  /**
   * WebSocket 연결 함수
   */
  const connect = () => {
    if (socketRef.current?.connected) {
      console.log('[useWebSocket] 이미 연결되어 있습니다.');
      return;
    }

    console.log('[useWebSocket] WebSocket 연결 시작:', { serverUrl, hasToken: !!accessToken });
    setConnectionStatus('connecting');

    try {
      // Socket.IO 클라이언트 생성
      const newSocket = io(serverUrl, {
        // 연결 옵션
        transports: ['websocket', 'polling'], // WebSocket 우선, fallback으로 polling
        timeout: 20000, // 연결 타임아웃 20초
        forceNew: true, // 새로운 연결 강제
        autoConnect: false, // 수동 연결 제어
        
        // 인증 정보
        auth: {
          token: accessToken // JWT 토큰 전송
        },
        
        // 추가 헤더
        extraHeaders: accessToken ? {
          'Authorization': `Bearer ${accessToken}`
        } : {},
        
        // 재연결 설정
        reconnection: autoReconnect,
        reconnectionAttempts: 5,
        reconnectionDelay: 1000,
        reconnectionDelayMax: 5000
      });

      // 이벤트 리스너 등록
      setupSocketEventListeners(newSocket);
      
      // 연결 시작
      newSocket.connect();
      
      // 상태 업데이트
      socketRef.current = newSocket;
      setSocket(newSocket);
      
    } catch (error) {
      console.error('[useWebSocket] 연결 생성 실패:', error);
      setConnectionStatus('error');
    }
  };

  /**
   * WebSocket 연결 해제 함수
   */
  const disconnect = () => {
    console.log('[useWebSocket] WebSocket 연결 해제');
    
    isManualDisconnectRef.current = true;
    
    // 재연결 타이머 정리
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    
    // 현재 페이지에서 나가기
    if (currentRoom && socketRef.current) {
      leavePage();
    }
    
    // 소켓 연결 해제
    if (socketRef.current) {
      socketRef.current.removeAllListeners();
      socketRef.current.disconnect();
      socketRef.current = null;
    }
    
    // 상태 업데이트
    setSocket(null);
    setConnectionStatus('disconnected');
    setCurrentRoom(null);
  };

  /**
   * 페이지 입장 함수
   */
  const joinPage = (workspaceId: string, pageId: string) => {
    if (!socketRef.current?.connected) {
      console.warn('[useWebSocket] 소켓이 연결되지 않음, 페이지 입장 불가');
      return;
    }

    const roomName = `${workspaceId}:${pageId}`;
    
    console.log('[useWebSocket] 페이지 입장:', { workspaceId, pageId, roomName });
    
    // 이전 페이지에서 나가기
    if (currentRoom && currentRoom !== roomName) {
      leavePage();
    }
    
    // 새 페이지 입장 (서버 이벤트명에 맞춤)
    socketRef.current.emit('page:join', {
      workspaceId,
      pageId,
      userId: accessToken ? 'current-user' : 'guest', // TODO: 실제 사용자 ID 사용
      timestamp: new Date().toISOString()
    });
    
    setCurrentRoom(roomName);
  };

  /**
   * 페이지 퇴장 함수
   */
  const leavePage = () => {
    if (!socketRef.current?.connected || !currentRoom) {
      return;
    }

    console.log('[useWebSocket] 페이지 퇴장:', { room: currentRoom });
    
    const [workspaceId, pageId] = currentRoom.split(':');
    
    // 서버 이벤트명에 맞춤
    socketRef.current.emit('page:leave', {
      workspaceId,
      pageId,
      userId: accessToken ? 'current-user' : 'guest', // TODO: 실제 사용자 ID 사용
      timestamp: new Date().toISOString()
    });
    
    setCurrentRoom(null);
  };

  /**
   * 블록 업데이트 전송 함수
   */
  const sendBlockUpdate = (blockData: any) => {
    if (!socketRef.current?.connected) {
      console.warn('[useWebSocket] 소켓이 연결되지 않음, 블록 업데이트 전송 불가');
      return;
    }

    console.log('[useWebSocket] 블록 업데이트 전송:', blockData);
    
    // 서버에서 실제로 리스닝하는 이벤트명 사용 (BLOCK_UPDATE_BROADCAST = 'block:update')
    socketRef.current.emit('block:update', {
      messageType: 'block', // MessageType enum 값 (소문자)
      messageId: `block-update-${Date.now()}`,
      timestamp: new Date().toISOString(),
      workspaceId: blockData.workspaceId,
      blockOperation: (blockData.operation || 'update').toLowerCase(), // BlockOperation enum 값 (소문자)
      blockDTO: blockData.block,
      userId: accessToken ? 'current-user' : 'guest' // TODO: 실제 사용자 ID 사용
    });
  };

  /**
   * Socket 이벤트 리스너 설정
   */
  const setupSocketEventListeners = (socket: Socket) => {
    // 연결 성공
    socket.on('connect', () => {
      console.log('[useWebSocket] 연결 성공:', socket.id);
      setConnectionStatus('connected');
      isManualDisconnectRef.current = false;
    });

    // 연결 해제
    socket.on('disconnect', (reason: string) => {
      console.log('[useWebSocket] 연결 해제:', reason);
      setConnectionStatus('disconnected');
      setCurrentRoom(null);
      
      // 수동 해제가 아니고 자동 재연결이 활성화된 경우 재연결 시도
      if (!isManualDisconnectRef.current && autoReconnect) {
        console.log('[useWebSocket] 자동 재연결 시도 예약');
        reconnectTimeoutRef.current = setTimeout(() => {
          if (!isManualDisconnectRef.current) {
            connect();
          }
        }, 3000);
      }
    });

    // 연결 오류
    socket.on('connect_error', (error: Error) => {
      console.error('[useWebSocket] 연결 오류:', error);
      setConnectionStatus('error');
    });

    // 인증 요청
    socket.on('authentication_required', () => {
      console.warn('[useWebSocket] 인증이 필요합니다');
      setConnectionStatus('error');
    });

    // 서버 오류
    socket.on('error', (error: any) => {
      console.error('[useWebSocket] 서버 오류:', error);
    });

    // 연결 완료 확인
    socket.on('connected', (data: any) => {
      console.log('[useWebSocket] 서버 연결 확인:', data);
    });

    // 사용자 입장 알림
    socket.on('user:joined', (data: any) => {
      console.log('[useWebSocket] 사용자 입장:', data);
    });

    // 사용자 퇴장 알림
    socket.on('user:left', (data: any) => {
      console.log('[useWebSocket] 사용자 퇴장:', data);
    });

    // 블록 업데이트 수신 (서버 이벤트명 수정)
    socket.on('block:update', (data: any) => {
      console.log('[useWebSocket] 블록 업데이트 수신:', data);
      // TODO: Yjs Document에 변경 사항 적용
    });
  };

  /**
   * 컴포넌트 언마운트 시 정리
   */
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, []);

  /**
   * 자동 연결
   */
  useEffect(() => {
    if (autoConnect && accessToken && !socketRef.current) {
      connect();
    }
  }, [autoConnect, accessToken]);

  /**
   * 토큰 변경 시 재연결
   */
  useEffect(() => {
    if (socketRef.current && accessToken) {
      // 토큰이 변경되면 재연결
      disconnect();
      setTimeout(() => {
        if (!isManualDisconnectRef.current) {
          connect();
        }
      }, 1000);
    }
  }, [accessToken]);

  return {
    socket,
    connectionStatus,
    isConnected: connectionStatus === 'connected',
    connect,
    disconnect,
    joinPage,
    leavePage,
    sendBlockUpdate
  };
};

export default useWebSocket;
