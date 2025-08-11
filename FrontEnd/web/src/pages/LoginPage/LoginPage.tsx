import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';
import { API_ENDPOINTS } from '../../constants/api';
import { getAccessToken, login } from '../../utils/authUtils';
import { useAuth } from '../../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  
  // useAuth()로 AuthContext의 모든 값을 가져옴
  const authContext = useAuth();
  
  // authContext에서 setUserWithToken 함수를 추출
  const setUserWithToken = authContext.setUserWithToken;

  /**
   * 로그인 폼 제출 시 호출되는 함수
   * @param e - 폼 이벤트 객체
   * @returns void
   * 내부 동작 순서:
   * 1. 폼 기본 동작 방지
   * 2. authUtils의 login 함수로 서버 로그인 시도
   * 3. 로그인 성공 시 AuthContext의 setUserWithToken으로 사용자 정보 설정
   * 4. 홈페이지로 이동
   */
  const handleLogin = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    
    if (isLoading) return; // 중복 요청 방지
    
    setIsLoading(true);
    
    try {
      // 1. authUtils의 login 함수로 서버 로그인 (토큰 획득)
      const loginSuccess = await login(id, pw);
      
      if (loginSuccess) {
        // 2. 로그인 성공 시 localStorage에서 토큰을 가져와서 AuthContext에 설정
        const token = getAccessToken();
        if (token) {
          await setUserWithToken(token);
          console.log('[LoginPage] AuthContext 사용자 정보 설정 완료');
          
          // 3. 홈페이지로 이동
          navigate('/');
        } else {
          console.error('[LoginPage] 로그인 성공했지만 토큰을 찾을 수 없음');
          alert('로그인 처리 중 오류가 발생했습니다.');
        }
      }
      // 로그인 실패 시 authUtils의 login 함수에서 이미 alert 처리됨
    } catch (error) {
      console.error('[LoginPage] AuthContext 사용자 정보 설정 중 오류:', error);
      alert('로그인 처리 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Google OAuth2 로그인 버튼 클릭 핸들러
   */
  const handleGoogleLogin = (): void => {
    // Google OAuth2 인증 시작 URL로 이동
    window.location.href = API_ENDPOINTS.OAUTH2_GOOGLE_LOGIN;
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
        <button type="submit" disabled={isLoading}>
          {isLoading ? '로그인 중...' : '로그인'}
        </button>
        
        {/* 구분선 */}
        <div className="login-divider">
          <span>또는</span>
        </div>
        
        {/* Google 로그인 버튼 */}
        <button 
          type="button" 
          className="google-login-button"
          onClick={handleGoogleLogin}
        >
          <svg className="google-icon" viewBox="0 0 24 24" width="18" height="18">
            <path fill="#4285f4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34a853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#fbbc05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#ea4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          Google로 로그인
        </button>
        
        <div className="login-signup-link">
          <span>계정이 없으신가요?</span>
          <button type="button" onClick={() => navigate('/signup')}>회원가입</button>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;
