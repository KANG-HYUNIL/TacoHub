import React from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div className="notion-home">
      <header className="notion-header">
        <button className="notion-login-btn" onClick={() => navigate('/login')}>로그인</button>
        <button className="notion-signup-btn" onClick={() => navigate('/signup')}>회원가입</button>
      </header>
      <main className="notion-main">
        <h1>모든 작업을 한 곳에서</h1>
        <p>팀의 지식, 프로젝트, 업무를 한 곳에서 관리하세요.</p>
        <div className="notion-visual-placeholder">
          {/* 실제 Notion의 이미지/비주얼은 저작권 문제로 임의 디자인 */}
          <div className="notion-visual-box">Notion 스타일 비주얼 영역</div>
        </div>
      </main>
    </div>
  );
};

export default HomePage;
