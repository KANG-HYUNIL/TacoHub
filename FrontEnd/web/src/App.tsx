import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';
import Footer from './components/Footer';
import Header from './components/Header/Header';
import HomePage from './pages/HomePage/HomePage';
import LoginPage from './pages/LoginPage/LoginPage';
import SignupPage from './pages/SignupPage/SignupPage';
import WorkspacePage from './pages/WorkspacePage/WorkspacePage';
import DescPage from './pages/DescPage/DescPage';
import OAuth2SuccessPage from './pages/OAuth2SuccessPage/OAuth2SuccessPage';
import { AuthProvider } from './contexts/AuthContext';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        {/* 여러 경로에 따라 다른 페이지 컴포넌트 반환 */}
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/desc" element={<DescPage />} />
          <Route path="/login/oauth2/success" element={<OAuth2SuccessPage />} />
          {/* 워크스페이스 라우팅 - 다양한 패턴 지원 */}
          <Route path="/workspace" element={<WorkspacePage />} />
          <Route path="/workspace/:workspaceId" element={<WorkspacePage />} />
          <Route path="/workspace/:workspaceId/page/:pageId" element={<WorkspacePage />} />
        </Routes>
      <Footer />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
