// 백엔드 및 웹소켓 서버의 DTO 타입을 원본 구조 그대로 반영

// BaseDateDTO
export interface BaseDateDTO {
  createdAt: string;
  updatedAt: string;
}

// AccountDto
export interface AccountDto {
  emailId: string;
  password?: string;
  name: string;
  role: string;
  provider?: string; // OAuth 제공자 (google, naver 등), 일반 회원가입은 undefined
  oauthId?: string; // OAuth 제공자에서 발급한 고유 ID, 일반 회원가입은 undefined
}

// LogInDto
export interface LogInDto {
  emailId: string;
  password: string;
}

// EmailVerificationDto
export interface EmailVerificationDto {
  email: string;
  authCode: string;
  purpose: string;
}

// ErrorResponseDTO
export interface ErrorResponseDTO {
  error: string;
  message: string;
  timestamp: string;
  details?: Record<string, string>;
}


