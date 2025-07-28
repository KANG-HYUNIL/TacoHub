import React, { useState } from 'react';
import './SignupPage.css';
import { API_ENDPOINTS } from '../../constants/api';
import axios from 'axios';

const SignupPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [authCode, setAuthCode] = useState('');
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isCodeVerified, setIsCodeVerified] = useState(false);
  const [pw, setPw] = useState('');
  const [pwCheck, setPwCheck] = useState('');
  const [isEmailChecked, setIsEmailChecked] = useState(false);
  const [emailCheckMsg, setEmailCheckMsg] = useState('');

  /**
   * 이메일 중복 검증 버튼 클릭 시 호출
   * @returns void
   * 1. 이메일 형식 및 빈 값 체크
   * 2. axios로 중복 체크 API 호출
   * 3. 성공 시 isEmailChecked true, 메시지 표시
   * 4. 실패 시 메시지 표시
   */
  const handleCheckEmail = async (): Promise<void> => {
    // 1. 이메일 형식 및 빈 값 체크
    if (!email) {
      setEmailCheckMsg('이메일을 입력하세요.');
      return;
    }
    // 2. axios로 중복 체크 API 호출
    try {
      const response = await axios.post<{ message?: string }>(API_ENDPOINTS.ACCOUNT_CHECK_EMAIL, { emailId: email });
      // 3. 성공 시 isEmailChecked true, 메시지 표시
      if (response.status === 200) {
        setIsEmailChecked(true);
        setEmailCheckMsg('사용 가능한 이메일입니다.');
      } else {
        setEmailCheckMsg(response.data.message || '이메일 중복 확인 실패');
      }
    } catch (error: any) {
      // 4. 실패 시 메시지 표시
      setEmailCheckMsg(error.response?.data?.message || '이메일 중복 확인 요청 실패');
    }
  };

  /**
   * 인증번호 전송 버튼 클릭 시 호출
   * @returns void
   * 1. 이메일 중복 검증 성공 여부 확인
   * 2. axios로 인증번호 전송 API 호출 (실제 구현 필요)
   * 3. 성공 시 isCodeSent true
   * 4. 실패 시 alert
   */
  const handleSendCode = async (): Promise<void> => {
    // 1. 이메일 중복 검증 성공 여부 확인
    if (!isEmailChecked) {
      alert('이메일 중복 검증을 먼저 완료하세요.');
      return;
    }
    // 2. axios로 인증번호 전송 API 호출 (실제 구현 필요)
    // TODO: 실제 API 연동 필요
    // 3. 성공 시 isCodeSent true
    setIsCodeSent(true);
    // 4. 실패 시 alert (실제 구현 시 catch에서 처리)
  };

  /**
   * 인증번호 확인 버튼 클릭 시 호출
   * @returns void
   * 1. 인증번호 입력값 체크
   * 2. axios로 인증번호 검증 API 호출 (실제 구현 필요)
   * 3. 성공 시 isCodeVerified true
   * 4. 실패 시 alert
   */
  const handleVerifyCode = async (): Promise<void> => {
    // 1. 인증번호 입력값 체크
    if (!authCode) {
      alert('인증번호를 입력하세요.');
      return;
    }
    // 2. axios로 인증번호 검증 API 호출 (실제 구현 필요)
    // TODO: 실제 API 연동 필요
    // 3. 성공 시 isCodeVerified true
    setIsCodeVerified(true);
    // 4. 실패 시 alert (실제 구현 시 catch에서 처리)
  };

  /**
   * 회원가입 폼 제출 시 호출
   * @param e - 폼 이벤트 객체
   * @returns void
   * 1. 비밀번호 일치 여부 확인
   * 2. axios로 회원가입 API 호출 (실제 구현 필요)
   * 3. 성공 시 알림
   * 4. 실패 시 알림
   */
  const handleSignup = (e: React.FormEvent): void => {
    // 1. 폼 기본 동작 방지
    e.preventDefault();
    // 2. 비밀번호 일치 여부 확인
    if (pw !== pwCheck) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    // 3. axios로 회원가입 API 호출 (실제 구현 필요)
    // TODO: 실제 API 연동 필요
    // 4. 성공 시 알림, 실패 시 알림 (실제 구현 시 catch에서 처리)
    alert('회원가입 완료! (실제 API 연동 필요)');
  };

  return (
    <div className="signup-container">
      <form className="signup-form" onSubmit={handleSignup}>
        <h2>회원가입</h2>
        <div className="email-check-row">
          <input
            type="email"
            placeholder="이메일(ID) 입력"
            value={email}
            onChange={e => {
              setEmail(e.target.value);
              setIsEmailChecked(false);
              setEmailCheckMsg('');
            }}
            required
          />
          <button type="button" onClick={handleCheckEmail} disabled={isEmailChecked || !email}>
            중복 검증
          </button>
        </div>
        {emailCheckMsg && <div className="email-check-msg">{emailCheckMsg}</div>}
        {/* 이메일 중복 검증 성공 시에만 인증번호 전송 버튼 노출 */}
        {isEmailChecked && !isCodeSent && (
          <button type="button" onClick={handleSendCode}>
            인증번호 전송
          </button>
        )}
        {/* 인증번호 전송 후 인증번호 입력 레이어 노출 */}
        {isCodeSent && (
          <>
            <input
              type="text"
              placeholder="인증번호 입력"
              value={authCode}
              onChange={e => setAuthCode(e.target.value)}
              required
            />
            <button type="button" onClick={handleVerifyCode} disabled={isCodeVerified || !authCode}>
              인증번호 확인
            </button>
          </>
        )}
        {/* 인증번호 검증 성공 시 비밀번호 입력 레이어 노출 */}
        {isCodeVerified && (
          <>
            <input
              type="password"
              placeholder="비밀번호 입력"
              value={pw}
              onChange={e => setPw(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="비밀번호 확인"
              value={pwCheck}
              onChange={e => setPwCheck(e.target.value)}
              required
            />
            <button type="submit">회원가입 완료</button>
          </>
        )}
      </form>
    </div>
  );
};

export default SignupPage;
