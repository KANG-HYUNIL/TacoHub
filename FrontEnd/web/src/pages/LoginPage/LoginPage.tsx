import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';
import { API_ENDPOINTS } from '../../constants/api';
import axios from 'axios';
import { LogInDto } from '../../types/dto';
import { setLocalStorage, saveAccessToken, login } from '../../utils/authUtils';

const LoginPage: React.FC = () => {
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');
  const navigate = useNavigate();

  /**
   * 로그인 폼 제출 시 호출되는 함수
   * @param e - 폼 이벤트 객체
   * @returns void
   * 내부 동작 순서:
   * 1. 폼 기본 동작 방지
   * 2. 입력값을 DTO로 변환
   * 3. axios로 로그인 API 호출 (withCredentials로 쿠키 저장)
   * 4. 응답 status 200이면 accessToken을 localStorage에 저장, 성공 알림
   * 5. 실패 시 서버 메시지 추출하여 에러 알림
   */
  const handleLogin = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    await login(id, pw);
  };

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleLogin}>
        <h2>로그인</h2>
        <input
          type="text"
          placeholder="아이디"
          value={id}
          onChange={e => setId(e.target.value)}
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={pw}
          onChange={e => setPw(e.target.value)}
        />
        <button type="submit">로그인</button>
        <div className="login-signup-link">
          <span>계정이 없으신가요?</span>
          <button type="button" onClick={() => navigate('/signup')}>회원가입</button>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;
