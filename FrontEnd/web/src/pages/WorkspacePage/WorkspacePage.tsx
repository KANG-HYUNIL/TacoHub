import React, { useState } from 'react';
import './WorkspacePage.css';

// TODO: 실제 데이터는 API/Socket으로 받아와야 함
const mockWorkspaces = [
  { id: 'ws1', name: '개인 워크스페이스' },
  { id: 'ws2', name: '팀 워크스페이스' },
];
const mockPages = [
  { id: 'p1', title: 'Root Page 1', children: [
    { id: 'p2', title: 'Child Page 1-1', children: [] },
    { id: 'p3', title: 'Child Page 1-2', children: [
      { id: 'p4', title: 'Sub Child Page', children: [] }
    ] }
  ] },
  { id: 'p5', title: 'Root Page 2', children: [] }
];

const WorkspacePage: React.FC = () => {
  const [selectedWorkspace, setSelectedWorkspace] = useState(mockWorkspaces[0]);
  const [workspaceListOpen, setWorkspaceListOpen] = useState(false);
  const [expandedPages, setExpandedPages] = useState<string[]>([]);
  const [selectedPage, setSelectedPage] = useState(mockPages[0]);

  // 페이지 트리 펼치기/접기
  const togglePage = (id: string) => {
    setExpandedPages(prev => prev.includes(id) ? prev.filter(e => e !== id) : [...prev, id]);
  };

  // 워크스페이스 변경
  const handleWorkspaceChange = (ws: typeof mockWorkspaces[0]) => {
    setSelectedWorkspace(ws);
    // TODO: 해당 워크스페이스의 페이지 목록/데이터 불러오기
  };

  // 페이지 선택
  const handlePageSelect = (page: typeof mockPages[0]) => {
    setSelectedPage(page);
    // TODO: 해당 페이지 데이터 불러오기 및 Yjs 연결
  };

  // 페이지 트리 렌더링
  const renderPageTree = (pages: any[]) => (
    <ul className="page-tree">
      {pages.map(page => (
        <li key={page.id}>
          <div className="page-item">
            {page.children.length > 0 && (
              <span className="arrow" onClick={() => togglePage(page.id)}>
                {expandedPages.includes(page.id) ? '▼' : '▶'}
              </span>
            )}
            <span className={`page-title${selectedPage.id === page.id ? ' selected' : ''}`} onClick={() => handlePageSelect(page)}>
              {page.title}
            </span>
          </div>
          {page.children.length > 0 && expandedPages.includes(page.id) && renderPageTree(page.children)}
        </li>
      ))}
    </ul>
  );

  return (
    <div className="workspace-container">
      <aside className="workspace-sidebar">
        <div className="workspace-header">
          <span className="workspace-name" onClick={() => setWorkspaceListOpen(!workspaceListOpen)}>
            {selectedWorkspace.name} ▾
          </span>
          {workspaceListOpen && (
            <ul className="workspace-list">
              {mockWorkspaces.map(ws => (
                <li key={ws.id} onClick={() => handleWorkspaceChange(ws)}>{ws.name}</li>
              ))}
            </ul>
          )}
        </div>
        <nav className="page-nav">
          {renderPageTree(mockPages)}
        </nav>
      </aside>
      <main className="workspace-main">
        <h2>{selectedPage.title}</h2>
        {/* Yjs 협업 에디터 영역 - 실제 구현 시 Yjs Editor 컴포넌트로 대체 */}
        <div className="yjs-editor-placeholder">
          {/* TODO: Yjs Editor 연결 및 데이터 표시 */}
          <p>여기에 해당 페이지의 데이터가 표시됩니다.</p>
        </div>
      </main>
    </div>
  );
};

export default WorkspacePage;
