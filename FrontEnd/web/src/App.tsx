import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';
import Footer from './components/Footer';
import HomePage from './pages/HomePage/HomePage';
import LoginPage from './pages/LoginPage/LoginPage';
import SignupPage from './pages/SignupPage/SignupPage';
import WorkspacePage from './pages/WorkspacePage/WorkspacePage';
import DescPage from './pages/DescPage/DescPage';

function App() {
  return (
    <BrowserRouter>
      {/* 여러 경로에 따라 다른 페이지 컴포넌트 반환 */}
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/desc" element={<DescPage />} />
        <Route path="/workspace/:workspaceId/:pageId" element={<WorkspacePage />} />
      </Routes>
    <Footer />
    </BrowserRouter>
  );
}

export default App;
