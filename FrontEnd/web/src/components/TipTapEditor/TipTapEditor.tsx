import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Collaboration from '@tiptap/extension-collaboration';
import * as Y from 'yjs';
import { useAuth } from '../../contexts/AuthContext';
import useWebSocket from '../../hooks/useWebSocket';
import { BlockDTO } from '../../types/notioncopy-dto';
import { PageService } from '../../services/pageService';
import './TipTapEditor.css';

/**
 * TipTap Editor 컴포넌트의 Props 타입
 */
interface TipTapEditorProps {
  /** Yjs Document 인스턴스 */
  yjsDoc: Y.Doc;
  /** 에디터가 읽기 전용인지 여부 */
  editable?: boolean;
  /** 에디터 내용이 변경될 때 호출되는 콜백 함수 */
  onUpdate?: () => void;
  /** 페이지 ID (Yjs 문서 식별용) */
  pageId: string;
  /** 워크스페이스 ID (WebSocket 연결용) */
  workspaceId?: string;
}

/**
 * TipTap + Yjs + Block 관리 협업 에디터 컴포넌트
 * 
 * @param yjsDoc - Yjs Document 인스턴스
 * @param editable - 편집 가능 여부 (기본값: true)
 * @param onUpdate - 에디터 내용 변경 시 콜백
 * @param pageId - 페이지 식별자
 * @param workspaceId - 워크스페이스 식별자
 * @returns JSX.Element
 */
const TipTapEditor: React.FC<TipTapEditorProps> = ({
  yjsDoc,
  editable = true,
  onUpdate,
  pageId,
  workspaceId
}) => {
  console.log('[TipTapEditor] 에디터 초기화 시작:', { pageId, workspaceId, editable });

  // 인증 정보 가져오기
  const { user } = useAuth();
  
  // 상태 관리
  const [blocks, setBlocks] = useState<BlockDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // WebSocket 연결 관리
  const {
    socket,
    connectionStatus,
    isConnected,
    connect,
    disconnect,
    joinPage,
    leavePage,
    sendBlockUpdate
  } = useWebSocket({
    serverUrl: process.env.REACT_APP_WEBSOCKET_URL || 'http://localhost:3001',
    accessToken: user ? localStorage.getItem('accessToken') : null,
    autoConnect: !!user, // 로그인된 사용자만 자동 연결
    autoReconnect: true
  });

  // WebSocket 페이지 입장/퇴장 관리
  useEffect(() => {
    if (isConnected && workspaceId && pageId) {
      console.log('[TipTapEditor] 페이지 입장:', { workspaceId, pageId });
      joinPage(workspaceId, pageId);
      
      return () => {
        console.log('[TipTapEditor] 페이지 퇴장:', { workspaceId, pageId });
        leavePage();
      };
    }
  }, [isConnected, workspaceId, pageId, joinPage, leavePage]);

  // 페이지 블록 데이터 로드
  useEffect(() => {
    const loadPageBlocks = async () => {
      if (!pageId) return;
      
      try {
        setLoading(true);
        setError(null);
        
        console.log('[TipTapEditor] 페이지 블록 로드 시작:', pageId);
        const pageBlocks = await PageService.getPageBlocks(pageId);
        
        console.log('[TipTapEditor] 페이지 블록 로드 완료:', {
          pageId,
          blockCount: pageBlocks.length
        });
        
        setBlocks(pageBlocks);
        
        // 블록 데이터를 Yjs Document에 초기화
        initializeYjsDocument(pageBlocks);
        
      } catch (error) {
        console.error('[TipTapEditor] 페이지 블록 로드 실패:', error);
        setError('페이지를 불러올 수 없습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadPageBlocks();
  }, [pageId]);

  /**
   * 블록 데이터를 Yjs Document에 초기화
   */
  const initializeYjsDocument = useCallback((blockData: BlockDTO[]) => {
    console.log('[TipTapEditor] Yjs Document 초기화:', { blockCount: blockData.length });
    
    const yjsContent = yjsDoc.getText('content');
    
    // 기존 내용이 있으면 초기화하지 않음 (다른 사용자가 이미 편집 중일 수 있음)
    if (yjsContent.length > 0) {
      console.log('[TipTapEditor] Yjs Document에 기존 내용 존재, 초기화 건너뜀');
      return;
    }
    
    // 블록 데이터를 HTML로 변환하여 Yjs에 설정
    const htmlContent = blocksToHtml(blockData);
    if (htmlContent) {
      yjsDoc.transact(() => {
        yjsContent.insert(0, htmlContent);
      });
      console.log('[TipTapEditor] Yjs Document 초기화 완료');
    }
  }, [yjsDoc]);

  /**
   * BlockDTO 배열을 HTML 문자열로 변환
   */
  const blocksToHtml = useCallback((blockData: BlockDTO[]): string => {
    return blockData
      .sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0))
      .map(block => {
        switch (block.blockType) {
          case 'heading1':
            return `<h1>${block.content}</h1>`;
          case 'heading2':
            return `<h2>${block.content}</h2>`;
          case 'heading3':
            return `<h3>${block.content}</h3>`;
          case 'paragraph':
            return `<p>${block.content}</p>`;
          case 'bulletList':
            return `<ul><li>${block.content}</li></ul>`;
          case 'numberedList':
            return `<ol><li>${block.content}</li></ol>`;
          default:
            return `<p>${block.content}</p>`;
        }
      })
      .join('');
  }, []);

  /**
   * HTML 콘텐츠를 BlockDTO 배열로 변환
   */
  const htmlToBlocks = useCallback((htmlContent: string): BlockDTO[] => {
    // 간단한 HTML 파싱 (실제로는 더 정교한 파서 필요)
    const parser = new DOMParser();
    const doc = parser.parseFromString(htmlContent, 'text/html');
    const elements = Array.from(doc.body.children);
    
    return elements.map((element, index): BlockDTO => {
      const tagName = element.tagName.toLowerCase();
      let blockType = 'paragraph';
      
      switch (tagName) {
        case 'h1':
          blockType = 'heading1';
          break;
        case 'h2':
          blockType = 'heading2';
          break;
        case 'h3':
          blockType = 'heading3';
          break;
        case 'ul':
          blockType = 'bulletList';
          break;
        case 'ol':
          blockType = 'numberedList';
          break;
        case 'p':
        default:
          blockType = 'paragraph';
          break;
      }
      
      return {
        id: `block-${Date.now()}-${index}`, // 임시 ID (서버에서 재할당)
        pageId: pageId,
        blockType: blockType,
        content: element.textContent || '',
        orderIndex: index,
        hasChildren: false
      };
    });
  }, [pageId]);

  /**
   * 에디터 내용 변경 처리
   */
  const handleContentChange = useCallback((editor: any) => {
    if (!workspaceId || !isConnected) return;
    
    // 현재 에디터 내용을 HTML로 가져오기
    const htmlContent = editor.getHTML();
    
    // HTML을 블록 데이터로 변환
    const newBlocks = htmlToBlocks(htmlContent);
    
    // 변경된 블록만 찾아서 WebSocket으로 전송
    newBlocks.forEach((newBlock, index) => {
      const existingBlock = blocks[index];
      
      // 새로운 블록이거나 내용이 변경된 경우
      if (!existingBlock || existingBlock.content !== newBlock.content || existingBlock.blockType !== newBlock.blockType) {
        sendBlockUpdate({
          workspaceId,
          operation: existingBlock ? 'update' : 'create', // 소문자로 변경
          block: newBlock
        });
      }
    });
    
    // 로컬 상태 업데이트
    setBlocks(newBlocks);
  }, [workspaceId, isConnected, blocks, htmlToBlocks, sendBlockUpdate]);

  // TipTap 에디터 설정
  const editor = useEditor({
    extensions: [
      // 기본 에디터 기능들 (Bold, Italic, Paragraph 등)
      StarterKit,
      
      // Yjs 협업 확장
      Collaboration.configure({
        // Yjs Document에서 'content' 필드를 사용 (Y.Text 타입)
        document: yjsDoc,
        // 협업 필드명 (한 문서에서 여러 협업 필드 사용 가능)
        field: 'content',
      }),
    ],
    
    // 에디터 설정
    editable: editable,
    
    // 에디터 내용 변경 시 콜백
    onUpdate: ({ editor }) => {
      console.log('[TipTapEditor] 에디터 내용 변경됨:', { 
        pageId, 
        contentLength: editor.getText().length,
        isConnected 
      });
      
      // 내용 변경 시 블록 데이터 업데이트 및 WebSocket 전송
      handleContentChange(editor);
      
      // 외부 콜백 함수 호출
      if (onUpdate) {
        onUpdate();
      }
    },
    
    // 에디터 생성 시 콜백
    onCreate: ({ editor }) => {
      console.log('[TipTapEditor] 에디터 생성 완료:', { 
        pageId,
        hasContent: editor.getText().length > 0,
        connectionStatus
      });
    },
    
    // 에디터가 포커스될 때 콜백
    onFocus: ({ editor }) => {
      console.log('[TipTapEditor] 에디터 포커스됨:', { pageId });
    },
    
    // 에디터가 포커스를 잃을 때 콜백
    onBlur: ({ editor }) => {
      console.log('[TipTapEditor] 에디터 포커스 해제됨:', { pageId });
    }
  });

  // WebSocket 이벤트 리스너 설정
  useEffect(() => {
    if (!socket) return;
    
    // 다른 사용자의 블록 업데이트 수신
    const handleBlockUpdate = (data: any) => {
      console.log('[TipTapEditor] 다른 사용자의 블록 업데이트 수신:', data);
      
      // TODO: 수신된 블록 데이터로 로컬 상태 및 에디터 내용 업데이트
      // 현재는 Yjs가 자동으로 동기화하므로 추가 처리 생략
    };
    
    socket.on('block:update', handleBlockUpdate);
    
    return () => {
      socket.off('block:update', handleBlockUpdate);
    };
  }, [socket]);

  // 컴포넌트 언마운트 시 WebSocket 정리
  useEffect(() => {
    return () => {
      if (isConnected) {
        leavePage();
      }
    };
  }, []);

  // 로딩 상태
  if (loading) {
    return (
      <div className="tiptap-editor-loading">
        <div className="loading-spinner"></div>
        <p>페이지를 불러오는 중...</p>
      </div>
    );
  }

  // 에러 상태
  if (error) {
    return (
      <div className="tiptap-editor-error">
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>다시 시도</button>
      </div>
    );
  }

  return (
    <div className="tiptap-editor-container">
      {/* 연결 상태 표시 */}
      <div className={`tiptap-connection-status ${connectionStatus}`}>
        <span className="status-indicator"></span>
        <span className="status-text">
          {connectionStatus === 'connected' && '실시간 협업 연결됨'}
          {connectionStatus === 'connecting' && '연결 중...'}
          {connectionStatus === 'disconnected' && (user ? '연결 끊김' : '오프라인 모드')}
          {connectionStatus === 'error' && '연결 오류'}
        </span>
        {user && connectionStatus !== 'connected' && (
          <button 
            onClick={connect} 
            className="reconnect-button"
            title="다시 연결"
          >
            🔄
          </button>
        )}
      </div>

      {/* 에디터 툴바 */}
      <div className="tiptap-toolbar">
        <button
          onClick={() => editor.chain().focus().toggleBold().run()}
          className={editor.isActive('bold') ? 'active' : ''}
          disabled={!editable}
          title="굵게"
        >
          <strong>B</strong>
        </button>
        
        <button
          onClick={() => editor.chain().focus().toggleItalic().run()}
          className={editor.isActive('italic') ? 'active' : ''}
          disabled={!editable}
          title="기울임"
        >
          <em>I</em>
        </button>
        
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          className={editor.isActive('heading', { level: 1 }) ? 'active' : ''}
          disabled={!editable}
          title="제목 1"
        >
          H1
        </button>
        
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          className={editor.isActive('heading', { level: 2 }) ? 'active' : ''}
          disabled={!editable}
          title="제목 2"
        >
          H2
        </button>
        
        <button
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          className={editor.isActive('bulletList') ? 'active' : ''}
          disabled={!editable}
          title="불릿 리스트"
        >
          • List
        </button>
        
        <button
          onClick={() => editor.chain().focus().undo().run()}
          disabled={!editor.can().undo() || !editable}
          title="실행 취소"
        >
          ↶ Undo
        </button>
        
        <button
          onClick={() => editor.chain().focus().redo().run()}
          disabled={!editor.can().redo() || !editable}
          title="다시 실행"
        >
          ↷ Redo
        </button>
      </div>

      {/* 에디터 본문 */}
      <div className="tiptap-editor-content">
        <EditorContent editor={editor} />
      </div>

      {/* 에디터 상태 정보 (개발용) */}
      <div className="tiptap-editor-status">
        <small>
          페이지: {pageId} | 
          편집 모드: {editable ? '편집 가능' : '읽기 전용'} |
          문자 수: {editor.getText().length} |
          블록 수: {blocks.length} |
          연결: {connectionStatus}
          {user && ` | 사용자: ${user.name}`}
        </small>
      </div>
    </div>
  );
};

export default TipTapEditor;
