import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as Y from 'yjs';
import { useAuth } from '../../contexts/AuthContext';
import { getUserWorkspaces } from '../../utils/api/workspaceUserApi';
import { WorkSpaceDTO, PageDTO } from '../../types/notioncopy-dto';
import TipTapEditor from '../../components/TipTapEditor/TipTapEditor';
import './WorkspacePage.css';

// 페이지 트리 구조를 위한 확장된 PageDTO 타입
interface PageTreeItem extends PageDTO {
  children: PageTreeItem[];
}

const WorkspacePage: React.FC = () => {
  // URL 파라미터 및 네비게이션
  const { workspaceId, pageId } = useParams<{ workspaceId: string; pageId?: string }>();
  const navigate = useNavigate();
  
  // 인증 컨텍스트
  const { user, logout } = useAuth();
  
  // 상태 관리
  const [workspaces, setWorkspaces] = useState<WorkSpaceDTO[]>([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState<WorkSpaceDTO | null>(null);
  const [workspaceListOpen, setWorkspaceListOpen] = useState(false);
  const [expandedPages, setExpandedPages] = useState<string[]>([]);
  const [selectedPage, setSelectedPage] = useState<PageDTO | null>(null);
  const [pageTree, setPageTree] = useState<PageTreeItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Yjs Document 관리
  const yjsDocRef = useRef<Y.Doc | null>(null);
  
  /**
   * 인증 상태 확인 및 리다이렉트
   * 로그인되지 않은 사용자는 /workspace 경로에서만 로그인 페이지로 리다이렉트
   * 특정 워크스페이스/페이지 경로는 비로그인 사용자도 접근 가능
   */
  useEffect(() => {
    // /workspace 경로 (workspaceId, pageId 없음)인 경우에만 로그인 확인
    if (!workspaceId && !pageId && !user) {
      console.log('[WorkspacePage] 비로그인 사용자의 /workspace 접근, 로그인 페이지로 리다이렉트');
      navigate('/login');
      return;
    }
    
    if (user) {
      console.log('[WorkspacePage] 사용자 인증 확인됨:', { 
        emailId: user.emailId,
        name: user.name 
      });
    } else {
      console.log('[WorkspacePage] 비로그인 상태로 특정 워크스페이스/페이지 접근');
    }
  }, [user, navigate, workspaceId, pageId]);

  /**
   * 사용자 워크스페이스 목록 로드 및 라우팅 처리
   */
  useEffect(() => {
    // 로그인된 사용자만 워크스페이스 목록 로드
    if (!user) {
      // 비로그인 사용자는 특정 워크스페이스/페이지 접근만 허용
      if (workspaceId && pageId) {
        console.log('[WorkspacePage] 비로그인 사용자의 특정 페이지 접근 허용:', {
          workspaceId,
          pageId
        });
        // TODO: 공개 워크스페이스/페이지 검증 로직 추가
        setLoading(false);
      }
      return;
    }
    
    const loadUserWorkspaces = async () => {
      try {
        setLoading(true);
        setError(null);
        
        console.log('[WorkspacePage] 사용자 워크스페이스 목록 로드 시작');
        const userWorkspaces = await getUserWorkspaces();
        
        console.log('[WorkspacePage] 워크스페이스 목록 로드 완료:', {
          count: userWorkspaces.length,
          workspaces: userWorkspaces
        });
        
        setWorkspaces(userWorkspaces);
        
        // 워크스페이스가 없는 경우
        if (userWorkspaces.length === 0) {
          console.log('[WorkspacePage] 사용자 워크스페이스가 없음');
          setLoading(false);
          return;
        }
        
        // 라우팅 처리
        if (!workspaceId && !pageId) {
          // /workspace 경로: 첫 번째 워크스페이스의 첫 번째 페이지로 리다이렉트
          const firstWorkspace = userWorkspaces[0];
          const firstPage = firstWorkspace.rootPageDTOS?.[0];
          
          if (firstPage) {
            console.log('[WorkspacePage] 기본 페이지로 리다이렉트:', {
              workspaceId: firstWorkspace.id,
              pageId: firstPage.id
            });
            navigate(`/workspace/${firstWorkspace.id}/page/${firstPage.id}`, { replace: true });
            return;
          } else {
            console.log('[WorkspacePage] 첫 번째 워크스페이스에 페이지가 없음');
            setSelectedWorkspace(firstWorkspace);
          }
        } else if (workspaceId) {
          // 특정 워크스페이스 접근
          const targetWorkspace = userWorkspaces.find(ws => ws.id === workspaceId);
          if (targetWorkspace) {
            setSelectedWorkspace(targetWorkspace);
            console.log('[WorkspacePage] URL 파라미터 워크스페이스 선택됨:', targetWorkspace);
          } else {
            console.warn('[WorkspacePage] 워크스페이스를 찾을 수 없음, 홈으로 리다이렉트:', workspaceId);
            navigate('/', { replace: true });
            return;
          }
        }
        
      } catch (error) {
        console.error('[WorkspacePage] 워크스페이스 목록 로드 실패:', error);
        setError('워크스페이스 목록을 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    
    loadUserWorkspaces();
  }, [user, workspaceId, pageId, navigate]);

  /**
   * 선택된 워크스페이스의 페이지 트리 구성 및 페이지 선택
   */
  useEffect(() => {
    if (!selectedWorkspace) {
      setPageTree([]);
      setSelectedPage(null);
      return;
    }
    
    console.log('[WorkspacePage] 페이지 트리 구성 시작:', selectedWorkspace);
    
    // rootPageDTOS를 PageTreeItem으로 변환
    const buildPageTree = (pages: PageDTO[]): PageTreeItem[] => {
      return pages.map(page => ({
        ...page,
        children: page.childPages ? buildPageTree(page.childPages) : []
      }));
    };
    
    const tree = buildPageTree(selectedWorkspace.rootPageDTOS || []);
    setPageTree(tree);
    
    // 페이지 선택 로직
    if (pageId && selectedWorkspace.rootPageDTOS) {
      // URL 파라미터로 지정된 페이지 선택
      const findPageById = (pages: PageDTO[], targetId: string): PageDTO | null => {
        for (const page of pages) {
          if (page.id === targetId) return page;
          if (page.childPages) {
            const found = findPageById(page.childPages, targetId);
            if (found) return found;
          }
        }
        return null;
      };
      
      const targetPage = findPageById(selectedWorkspace.rootPageDTOS, pageId);
      if (targetPage) {
        setSelectedPage(targetPage);
        console.log('[WorkspacePage] URL 파라미터 페이지 선택됨:', targetPage);
      } else {
        console.warn('[WorkspacePage] 페이지를 찾을 수 없음, 홈으로 리다이렉트:', pageId);
        navigate('/', { replace: true });
        return;
      }
    } else if (selectedWorkspace.rootPageDTOS && selectedWorkspace.rootPageDTOS.length > 0) {
      // 기본적으로 첫 번째 페이지 선택 (pageId가 없는 경우)
      const firstPage = selectedWorkspace.rootPageDTOS[0];
      setSelectedPage(firstPage);
      
      // URL 업데이트 (pageId가 없는 /workspace/:workspaceId 경로인 경우)
      if (!pageId && user) {
        console.log('[WorkspacePage] 첫 번째 페이지로 URL 업데이트:', firstPage);
        navigate(`/workspace/${selectedWorkspace.id}/page/${firstPage.id}`, { replace: true });
      }
    }
    
  }, [selectedWorkspace, pageId, navigate, user]);

  /**
   * Yjs Document 초기화 및 관리
   */
  useEffect(() => {
    if (!selectedPage) {
      // 선택된 페이지가 없으면 Yjs Document 정리
      if (yjsDocRef.current) {
        yjsDocRef.current.destroy();
        yjsDocRef.current = null;
      }
      return;
    }
    
    console.log('[WorkspacePage] Yjs Document 초기화:', selectedPage);
    
    // 기존 Document 정리
    if (yjsDocRef.current) {
      yjsDocRef.current.destroy();
    }
    
    // 새로운 Yjs Document 생성
    const yjsDoc = new Y.Doc();
    yjsDocRef.current = yjsDoc;
    
    // Document 변경 사항 감지
    yjsDoc.on('update', (update: Uint8Array) => {
      console.log('[WorkspacePage] Yjs Document 업데이트:', {
        pageId: selectedPage.id,
        updateSize: update.length
      });
      
      // TODO: WebSocket을 통해 다른 사용자들에게 변경 사항 전송
    });
    
    // TODO: WebSocket 연결 및 초기 데이터 동기화
    // 현재는 로컬에서만 동작
    
    return () => {
      // 컴포넌트 언마운트 시 Document 정리
      if (yjsDocRef.current) {
        yjsDocRef.current.destroy();
        yjsDocRef.current = null;
      }
    };
  }, [selectedPage]);

  // 페이지 트리 펼치기/접기
  const togglePage = (id: string) => {
    setExpandedPages(prev => 
      prev.includes(id) 
        ? prev.filter(expandedId => expandedId !== id) 
        : [...prev, id]
    );
  };

  // 워크스페이스 변경
  const handleWorkspaceChange = (workspace: WorkSpaceDTO) => {
    console.log('[WorkspacePage] 워크스페이스 변경:', workspace);
    setSelectedWorkspace(workspace);
    setWorkspaceListOpen(false);
    
    // 첫 번째 페이지로 URL 업데이트
    const firstPage = workspace.rootPageDTOS?.[0];
    if (firstPage) {
      navigate(`/workspace/${workspace.id}/page/${firstPage.id}`);
    } else {
      navigate(`/workspace/${workspace.id}`);
    }
  };

  // 페이지 선택
  const handlePageSelect = (page: PageDTO) => {
    console.log('[WorkspacePage] 페이지 선택:', page);
    setSelectedPage(page);
    
    // URL 업데이트
    if (selectedWorkspace) {
      navigate(`/workspace/${selectedWorkspace.id}/page/${page.id}`);
    }
  };

  // 페이지 트리 렌더링
  const renderPageTree = (pages: PageTreeItem[]): React.ReactElement => (
    <ul className="page-tree">
      {pages.map(page => (
        <li key={page.id}>
          <div className="page-item">
            {page.children.length > 0 && (
              <span 
                className="arrow" 
                onClick={() => togglePage(page.id)}
                title={expandedPages.includes(page.id) ? '접기' : '펼치기'}
              >
                {expandedPages.includes(page.id) ? '▼' : '▶'}
              </span>
            )}
            <span 
              className={`page-title${selectedPage?.id === page.id ? ' selected' : ''}`} 
              onClick={() => handlePageSelect(page)}
              title={page.title}
            >
              {page.title}
            </span>
          </div>
          {page.children.length > 0 && expandedPages.includes(page.id) && renderPageTree(page.children)}
        </li>
      ))}
    </ul>
  );

  // 에디터 업데이트 핸들러
  const handleEditorUpdate = () => {
    if (!selectedPage) return;
    
    console.log('[WorkspacePage] 에디터 내용 업데이트됨:', {
      pageId: selectedPage.id,
      pageTitle: selectedPage.title
    });
    
    // TODO: 자동 저장 로직 구현
    // 예: 디바운스된 저장 함수 호출
  };

  // 로딩 상태
  if (loading) {
    return (
      <div className="workspace-container loading">
        <div className="loading-content">
          <div className="loading-spinner"></div>
          <p>워크스페이스를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  // 에러 상태
  if (error) {
    return (
      <div className="workspace-container error">
        <div className="error-content">
          <h2>오류가 발생했습니다</h2>
          <p>{error}</p>
          <button onClick={() => window.location.reload()}>다시 시도</button>
        </div>
      </div>
    );
  }

  // 워크스페이스가 없는 경우 (로그인된 사용자만)
  if (user && workspaces.length === 0) {
    return (
      <div className="workspace-container empty">
        <div className="empty-content">
          <h2>워크스페이스가 없습니다</h2>
          <p>새 워크스페이스를 만들거나 기존 워크스페이스에 초대받으세요.</p>
          <button onClick={() => navigate('/workspace/create')}>워크스페이스 만들기</button>
        </div>
      </div>
    );
  }

  return (
    <div className="workspace-container">
      {/* 로그인된 사용자만 사이드바 표시 */}
      {user && (
        <aside className="workspace-sidebar">
          <div className="workspace-header">
            <span 
              className="workspace-name" 
              onClick={() => setWorkspaceListOpen(!workspaceListOpen)}
              title="워크스페이스 변경"
            >
              {selectedWorkspace?.name || '워크스페이스 선택'} ▾
            </span>
            {workspaceListOpen && (
              <ul className="workspace-list">
                {workspaces.map(workspace => (
                  <li 
                    key={workspace.id} 
                    onClick={() => handleWorkspaceChange(workspace)}
                    className={selectedWorkspace?.id === workspace.id ? 'active' : ''}
                  >
                    {workspace.name}
                  </li>
                ))}
              </ul>
            )}
          </div>
          <nav className="page-nav">
            {pageTree.length > 0 ? (
              renderPageTree(pageTree)
            ) : (
              <div className="no-pages">
                <p>페이지가 없습니다</p>
                <button>새 페이지 만들기</button>
              </div>
            )}
          </nav>
        </aside>
      )}
      
      <main className={`workspace-main ${!user ? 'full-width' : ''}`}>
        {selectedPage ? (
          <>
            <div className="page-header">
              <h1 className="page-title">{selectedPage.title}</h1>
              <div className="page-info">
                <span>페이지 ID: {selectedPage.id}</span>
                <span>워크스페이스: {selectedPage.workspaceName}</span>
                {!user && <span className="guest-mode">게스트 모드</span>}
              </div>
            </div>
            
            <div className="editor-container">
              {yjsDocRef.current ? (
                <TipTapEditor
                  yjsDoc={yjsDocRef.current}
                  pageId={selectedPage.id}
                  workspaceId={selectedWorkspace?.id}
                  editable={!!user} // 로그인된 사용자만 편집 가능
                  onUpdate={handleEditorUpdate}
                />
              ) : (
                <div className="editor-loading">
                  <p>에디터를 초기화하는 중...</p>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="no-page-selected">
            <h2>페이지를 선택하세요</h2>
            <p>{user ? '사이드바에서 편집할 페이지를 선택하세요.' : '유효한 페이지 URL로 접근해주세요.'}</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default WorkspacePage;
