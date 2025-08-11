import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Header.css';

/**
 * Header 컴포넌트
 * 사용자 인증 상태에 따라 다른 UI를 표시하는 상단 헤더
 * 
 * @returns JSX.Element
 */
const Header: React.FC = () => {
  // useAuth()로 AuthContext의 모든 값을 가져옴
  const authContext = useAuth();
  const navigate = useNavigate();
  
  // authContext에서 필요한 값들을 각각 추출
  const user = authContext.user;
  const isAuthenticated = authContext.isAuthenticated;
  const loading = authContext.loading;
  const logout = authContext.logout;

  /**
   * 로그아웃 버튼 클릭 핸들러
   * 로그아웃 처리 후 홈페이지로 이동
   */
  const handleLogout = (): void => {
    logout();
    navigate('/');
    alert('로그아웃되었습니다.');
  };

  // 로딩 중일 때 표시할 UI
  if (loading) {
    return (
      <header className="header">
        <div className="header-container">
          <Link to="/" className="logo">
            TacoHub
          </Link>
          <div className="header-right">
            <span className="loading-text">로딩 중...</span>
          </div>
        </div>
      </header>
    );
  }

  return (
    <header className="header">
      <div className="header-container">
        {/* 로고 영역 */}
        <Link to="/" className="logo">
          TacoHub
        </Link>

        {/* 네비게이션 메뉴 */}
        <nav className="nav-menu">
          <Link to="/" className="nav-link">홈</Link>
          <Link to="/desc" className="nav-link">소개</Link>
        </nav>

        {/* 사용자 영역 */}
        <div className="header-right">
          {isAuthenticated ? (
            // 로그인된 상태
            <div className="user-info">
              <span className="welcome-text">
                안녕하세요, <strong>{user?.name}</strong>님!
              </span>
              <span className="user-email">({user?.emailId})</span>
              <button 
                onClick={handleLogout}
                className="logout-button"
              >
                로그아웃
              </button>
            </div>
          ) : (
            // 로그인되지 않은 상태
            <div className="auth-buttons">
              <Link to="/login" className="login-link">
                로그인
              </Link>
              <Link to="/signup" className="signup-link">
                회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
