import React, { useState } from 'react';
import './SignupPage.css';
import { API_ENDPOINTS } from '../../constants/api';
import axios from 'axios';
import { signup, login } from '../../utils/authUtils';
import { createWorkspace } from '../../utils/api/workspaceApi';

const SignupPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [authCode, setAuthCode] = useState('');
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [pw, setPw] = useState('');
  const [pwCheck, setPwCheck] = useState('');
  const [isEmailChecked, setIsEmailChecked] = useState(false);
  const [emailCheckMsg, setEmailCheckMsg] = useState('');
  const [name, setName] = useState('');

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
   * 회원가입 폼 제출 시 호출
   * @param e - 폼 이벤트 객체
   * @returns void
   * 1. 비밀번호 일치 여부 확인
   * 2. axios로 회원가입 API 호출 (실제 구현 필요)
   * 3. 성공 시 알림
   * 4. 실패 시 알림
   */
  /**
   * 회원가입 폼 제출 시 호출
   * @param e - 폼 이벤트 객체
   * @returns void
   * 1. 폼 기본 동작 방지
   * 2. 비밀번호 일치 여부 확인
   * 3. 회원가입 API 호출 (authUtils.signup)
   * 4. 회원가입 성공 시 자동 로그인 (authUtils.login)
   * 5. 실패 시 알림
   */
  const handleSignup = async (e: React.FormEvent): Promise<void> => {
    // 1. 폼 기본 동작 방지
    e.preventDefault();
    // 2. 비밀번호 일치 여부 확인
    if (pw !== pwCheck) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    // 3. 회원가입 API 호출
    //TODO : 전부 고쳐야 함

  // 1. 회원가입
  const signupResult: boolean = await signup(email, pw, name, authCode);
  if (!signupResult) {
    alert('회원가입에 실패했습니다. 입력 정보를 확인해주세요.');
    return;
  }

  // 2. 로그인
  const loginResult: boolean = await login(email, pw);
  if (!loginResult) {
    alert('회원가입은 성공했으나, 자동 로그인에 실패했습니다. 로그인 페이지에서 다시 시도해주세요.');
    return;
  }

  // 3. 워크스페이스 생성
  const createWorkspaceResult: boolean = await createWorkspace('Default Workspace');
  if (!createWorkspaceResult) {
    alert('회원가입 및 로그인은 성공했으나, 워크스페이스 생성에 실패했습니다. 마이페이지에서 직접 생성할 수 있습니다.');
    return;
  }

  // 4. 최종 완료
  alert('회원가입이 완료되었습니다! 기본 워크스페이스가 생성되었으며, 자동으로 로그인되었습니다.');
  // 필요하다면 메인 페이지로 이동 등 추가 처리

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
        {/* 이메일 중복 검증 성공 시 인증번호 입력 및 비밀번호 입력 레이어 노출 */}
        {isEmailChecked && (
          <>
            <input
              type="text"
              placeholder="인증번호 입력"
              value={authCode}
              onChange={e => setAuthCode(e.target.value)}
              required
            />
            <input
              type="text"
              placeholder="이름 입력"
              value={name}
              onChange={e => setName(e.target.value)}
              required
            />
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
