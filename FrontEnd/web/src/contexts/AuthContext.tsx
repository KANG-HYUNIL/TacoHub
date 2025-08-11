import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { AccountDto } from '../types/dto';
import { fetchAccountInfo, getAccessToken, saveAccessToken } from '../utils/authUtils';

/**
 * AuthContext에서 제공할 데이터 타입 정의
 * 전역 인증 상태 관리를 위한 Context 타입
 */
interface AuthContextType {
  /** 현재 로그인한 사용자 정보 (로그인 안되면 null) */
  user: AccountDto | null;
  
  /** 로그인 상태 여부 (user가 null이 아니면 true) */
  isAuthenticated: boolean;
  
  /** 사용자 정보를 불러오는 중인지 여부 (앱 초기 로딩 시 true) */
  loading: boolean;
  
  /**
   * 토큰을 받아서 사용자 정보를 설정하는 함수
   * @param token - localStorage에 저장할 access token
   * @returns Promise<void>
   */
  setUserWithToken: (token: string) => Promise<void>;
  
  /**
   * 로그아웃 처리 함수 (localStorage 토큰 제거 및 사용자 상태 초기화)
   * @returns void
   */
  logout: () => void;
  
  /**
   * 사용자 정보 새로고침 함수 (현재 토큰으로 사용자 정보 다시 요청)
   * @returns Promise<void>
   */
  refreshUserInfo: () => Promise<void>;
}

/**
 * AuthContext 생성 (초기값은 기본값으로 설정)
 * 전역 인증 상태를 관리하는 React Context
 */
const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  loading: true,
  setUserWithToken: async () => {},
  logout: () => {},
  refreshUserInfo: async () => {},
});

/**
 * AuthProvider 컴포넌트의 props 타입
 */
interface AuthProviderProps {
  /** AuthProvider로 감쌀 모든 자식 컴포넌트들 */
  children: ReactNode;
}

/**
 * AuthProvider: 실제 인증 상태를 관리하고 Context에 제공하는 컴포넌트
 * 앱 전체를 감싸서 모든 하위 컴포넌트에서 인증 상태에 접근할 수 있게 함
 * 
 * @param children - AuthProvider로 감쌀 자식 컴포넌트들
 * @returns JSX.Element
 */
export function AuthProvider({ children }: AuthProviderProps) {
  /** 현재 로그인한 사용자 정보 상태 */
  const [user, setUser] = useState<AccountDto | null>(null);
  
  /** 앱 초기 로딩 상태 (사용자 정보를 확인하는 동안 true) */
  const [loading, setLoading] = useState<boolean>(true);

  // 앱이 시작될 때 localStorage에 저장된 토큰으로 사용자 정보 확인
  useEffect(() => {
    checkInitialAuth();
  }, []);

  /**
   * 초기 인증 상태 확인 함수
   * 앱 시작 시 localStorage의 토큰으로 사용자 정보를 확인
   * 
   * @returns Promise<void>
   */
  const checkInitialAuth = async (): Promise<void> => {
    console.log('[AuthProvider] 초기 인증 상태 확인 시작');
    
    // 1. localStorage에서 access token 가져오기
    const accessToken: string | null = getAccessToken();
    console.log('[AuthProvider] localStorage에서 토큰 확인:', accessToken ? '토큰 존재' : '토큰 없음');
    
    if (accessToken) {
      // 2. 토큰이 있으면 사용자 정보 요청
      try {
        console.log('[AuthProvider] 사용자 정보 요청 시작');
        const userInfo: AccountDto | null = await fetchAccountInfo();
        
        if (userInfo) {
          console.log('[AuthProvider] 사용자 정보 획득 성공:', userInfo);
          setUser(userInfo);
        } else {
          console.log('[AuthProvider] 사용자 정보 획득 실패 - 토큰이 유효하지 않음');
          // 유효하지 않은 토큰이면 localStorage에서 제거
          localStorage.removeItem('access');
          setUser(null);
        }
      } catch (error) {
        console.error('[AuthProvider] 사용자 정보 요청 중 오류:', error);
        // 오류 발생 시 토큰 제거
        localStorage.removeItem('access');
        setUser(null);
      }
    } else {
      console.log('[AuthProvider] 토큰이 없어서 사용자 정보 요청하지 않음');
      setUser(null);
    }
    
    // 3. 로딩 상태 해제
    setLoading(false);
    console.log('[AuthProvider] 초기 인증 상태 확인 완료');
  };

  /**
   * 토큰을 받아서 사용자 정보를 설정하는 함수
   * 로그인 성공 후 호출하여 인증 상태를 업데이트
   * 
   * @param token - localStorage에 저장할 access token
   * @returns Promise<void>
   * @throws Error - 사용자 정보를 가져올 수 없는 경우
   */
  const setUserWithToken = async (token: string): Promise<void> => {
    console.log('[AuthProvider] 사용자 정보 설정 시작');
    
    // 1. 토큰을 localStorage에 저장
    saveAccessToken(token);
    console.log('[AuthProvider] 토큰 localStorage에 저장 완료');
    
    // 2. 사용자 정보 요청
    try {
      console.log('[AuthProvider] 토큰으로 사용자 정보 요청 시작');
      const userInfo: AccountDto | null = await fetchAccountInfo();
      
      if (userInfo) {
        console.log('[AuthProvider] 사용자 정보 획득 성공:', userInfo);
        setUser(userInfo);
      } else {
        console.log('[AuthProvider] 사용자 정보 획득 실패');
        // 사용자 정보를 가져올 수 없으면 토큰도 제거
        localStorage.removeItem('access');
        setUser(null);
        throw new Error('사용자 정보를 가져올 수 없습니다');
      }
    } catch (error) {
      console.error('[AuthProvider] 사용자 정보 요청 중 오류:', error);
      localStorage.removeItem('access');
      setUser(null);
      throw error;
    }
  };

  /**
   * 로그아웃 처리 함수
   * localStorage에서 토큰을 제거하고 사용자 상태를 초기화
   * 
   * @returns void
   */
  const logout = (): void => {
    console.log('[AuthProvider] 로그아웃 처리 시작');
    
    // 1. localStorage에서 토큰 제거
    localStorage.removeItem('access');
    console.log('[AuthProvider] localStorage에서 토큰 제거 완료');
    
    // 2. 사용자 상태 초기화
    setUser(null);
    console.log('[AuthProvider] 사용자 상태 초기화 완료');
  };

  /**
   * 사용자 정보 새로고침 함수
   * 현재 저장된 토큰으로 사용자 정보를 다시 요청
   * 
   * @returns Promise<void>
   */
  const refreshUserInfo = async (): Promise<void> => {
    console.log('[AuthProvider] 사용자 정보 새로고침 시작');
    
    const accessToken: string | null = getAccessToken();
    
    if (accessToken) {
      try {
        console.log('[AuthProvider] 사용자 정보 새로고침 요청 시작');
        const userInfo: AccountDto | null = await fetchAccountInfo();
        
        if (userInfo) {
          console.log('[AuthProvider] 사용자 정보 새로고침 성공:', userInfo);
          setUser(userInfo);
        } else {
          console.log('[AuthProvider] 사용자 정보 새로고침 실패');
          localStorage.removeItem('access');
          setUser(null);
        }
      } catch (error) {
        console.error('[AuthProvider] 사용자 정보 새로고침 중 오류:', error);
        localStorage.removeItem('access');
        setUser(null);
      }
    } else {
      console.log('[AuthProvider] 토큰이 없어서 새로고침 불가');
      setUser(null);
    }
  };

  /** 로그인 상태 계산 (user가 null이 아니면 로그인 상태) */
  const isAuthenticated: boolean = user !== null;

  /**
   * Context에 제공할 값들
   * 하위 컴포넌트에서 useAuth()로 접근할 수 있는 데이터와 함수들
   */
  const contextValue: AuthContextType = {
    user,
    isAuthenticated,
    loading,
    setUserWithToken,
    logout,
    refreshUserInfo,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth 훅: AuthContext를 사용하기 위한 커스텀 훅
 * 컴포넌트에서 인증 상태와 관련 함수들에 접근할 때 사용
 * 
 * @returns AuthContextType - 인증 상태와 관련 함수들
 * @throws Error - AuthProvider 외부에서 사용하려고 할 때
 * 
 * @example
 * function MyComponent() {
 *   const { user, isAuthenticated, setUserWithToken, logout } = useAuth();
 *   
 *   if (isAuthenticated) {
 *     return <div>안녕하세요, {user?.name}님!</div>;
 *   } else {
 *     return <div>로그인해주세요</div>;
 *   }
 * }
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  
  // AuthProvider 외부에서 사용하려고 하면 에러 발생
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다');
  }
  
  return context;
}
