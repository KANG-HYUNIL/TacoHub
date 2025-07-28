import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';
import { API_ENDPOINTS } from '../../constants/api';
import axios from 'axios';
import { LogInDto } from '../../types/dto';

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
    // 1. 폼 기본 동작 방지
    e.preventDefault();
    // 2. 입력값을 DTO로 변환
    const loginDto : LogInDto = {
      emailId: id,
      password: pw,
    };

    try {
      // 3. axios로 로그인 API 호출 (withCredentials로 쿠키 저장)
      const response = await axios.post<{ message?: string }>(API_ENDPOINTS.LOGIN, loginDto, {
        headers: {
          'Content-Type': 'application/json',
        },
        withCredentials: true,
      });

      // 4. 응답 status 200이면 accessToken을 localStorage에 저장, 성공 알림
      if (response.status === 200) {
        const accessToken = response.headers['access'];
        if (accessToken) {
          localStorage.setItem('accessToken', accessToken);
        }
        alert('로그인 성공!');
      } else {
        // 5. 실패 시 서버 메시지 추출하여 에러 알림
        let errorMsg = '로그인 실패';
        if (response.data && typeof response.data.message === 'string') {
          errorMsg = response.data.message;
        }
        throw new Error(errorMsg);
      }
    } catch (error: any) {
      // 5. 실패 시 서버 메시지 추출하여 에러 알림
      let errorMsg = '로그인 요청 실패';
      if (error.response && error.response.data && typeof error.response.data.message === 'string') {
        errorMsg = error.response.data.message;
      } else if (error.message) {
        errorMsg = error.message;
      }
      alert(errorMsg);
    }
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
