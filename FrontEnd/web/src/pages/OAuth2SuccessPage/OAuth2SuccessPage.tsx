import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './OAuth2SuccessPage.css';
import { saveAccessToken } from '../../utils/authUtils';
import { useAuth } from '../../contexts/AuthContext';

const OAuth2SuccessPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  
  // useAuth()로 AuthContext의 모든 값을 가져옴
  const authContext = useAuth();
  
  // authContext에서 setUserWithToken 함수를 추출
  const setUserWithToken = authContext.setUserWithToken;

  useEffect(() => {
    const processOAuth2Login = async () => {
      try {
        // 쿠키에서 access token 획득
        const accessToken = getCookieValue('access');
        
        if (accessToken) {
          // 1. authUtils의 saveAccessToken을 통해 localStorage에 저장
          saveAccessToken(accessToken);
          console.log('[OAuth2SuccessPage] 토큰을 localStorage에 저장 완료');
          
          // 2. AuthContext의 setUserWithToken으로 사용자 정보 설정
          await setUserWithToken(accessToken);
          console.log('[OAuth2SuccessPage] AuthContext 사용자 정보 설정 완료');
          
          // 3. 쿠키에서 토큰 제거 (보안상 localStorage로 이동)
          document.cookie = 'access=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
          
          // 4. 로딩 상태 해제 후 홈으로 이동
          setIsLoading(false);
          
          setTimeout(() => {
            navigate('/');
          }, 1000); // 1초 후 이동 (사용자가 성공을 인지할 수 있도록)
        } else {
          // 토큰이 없으면 로그인 페이지로 이동
          console.error('[OAuth2SuccessPage] 쿠키에서 토큰을 찾을 수 없음');
          alert('로그인 정보를 찾을 수 없습니다.');
          navigate('/login');
        }
      } catch (error) {
        console.error('[OAuth2SuccessPage] OAuth2 로그인 처리 중 오류:', error);
        alert('로그인 처리 중 오류가 발생했습니다.');
        navigate('/login');
      }
    };

    processOAuth2Login();
  }, [navigate, setUserWithToken]);

  /**
   * 쿠키에서 특정 값을 가져오는 함수
   */
  const getCookieValue = (name: string): string | null => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      return parts.pop()?.split(';').shift() || null;
    }
    return null;
  };

  if (isLoading) {
    return (
      <div className="oauth2-success-container">
        <div className="oauth2-success-content">
          <div className="loading-spinner"></div>
          <h2>로그인 중...</h2>
          <p>Google 로그인을 처리하고 있습니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="oauth2-success-container">
      <div className="oauth2-success-content">
        <div className="success-icon">✓</div>
        <h2>로그인 성공!</h2>
        <p>홈페이지로 이동합니다...</p>
      </div>
    </div>
  );
};

export default OAuth2SuccessPage;
