// API endpoint 및 이벤트 명 예시 (현업에서는 enum 또는 const 객체 사용)
export const API_ENDPOINTS = {
  LOGIN: '/api/login', // SecurityConfig
  ACCOUNT_SIGNUP: '/account/postSignup', // AccountController
  ACCOUNT_CHECK_EMAIL: '/account/postCheckEmailId', // AccountController
  LOGOUT: '/logout', // SecurityConfig
  REFRESH_TOKEN: '/api/auth/refresh', // JwtController - 토큰 재발급 (POST)
  // ...추가 필요시 계속 반영
};


