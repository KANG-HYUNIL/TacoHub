// API endpoint 및 이벤트 명 예시 (현업에서는 enum 또는 const 객체 사용)

/**
 * 각종 API 엔드포인트 및 이벤트 명을 정의합니다.
 * 실제 운영 환경에 맞게 수정이 필요합니다.
 * 
 */
export const API_ENDPOINTS = {

  // ACCOUNT 관련 API
  LOGIN: '/api/login', // SecurityConfig
  ACCOUNT_SIGNUP: '/account/postSignup/{authCode}/{purpose}', // AccountController 회원가입 (authCode, purpose 경로 변수)
  ACCOUNT_CHECK_EMAIL: '/account/postCheckEmailId', // AccountController
  ACCOUNT_GET_INFO: '/account/getAccountInfo', // AccountController
  LOGOUT: '/logout', // SecurityConfig
  
  // JWT TOKEN 관련 API
  REFRESH_TOKEN: '/api/auth/refresh', // JwtController - 토큰 재발급 (POST)
  // ...추가 필요시 계속 반영


  // WORKSPACE 관련 API
  WORKSPACE_CREATE : '/api/workspaces', // WorkSpaceController - 워크스페이스 생성 (POST)

};


