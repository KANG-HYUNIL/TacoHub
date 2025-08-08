import { AccountDto, ErrorResponseDTO } from './dto';
import { WorkSpaceDTO } from './notioncopy-dto';

// 공통 API 응답 래퍼
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  timestamp?: string;
  errorCode?: string;
}

/**
 * Error 응답 공통 API Type
 */
export type ErrorResponse = ApiResponse<ErrorResponseDTO | null>;


// AccountController API 타입

/**
 * /account/getAccountInfo - access token으로 계정 정보 조회
 */
export interface GetAccountInfoRequest {}
export type GetAccountInfoResponse = ApiResponse<AccountDto | null>;

/**
 * /account/postSignup - 회원가입
 */
export interface PostSignupRequest {
  emailId: string;
  password: string;
  name: string;
  role?: string;
}
export type PostSignupResponse = ApiResponse<string | null>;

/**
 * /account/postCheckEmailId - 이메일 중복 확인
 */
export interface PostCheckEmailIdRequest {
  emailId: string;
}
export type PostCheckEmailIdResponse = ApiResponse<string | null>;

// JwtController API 타입

/**
 * /api/auth/refresh - refresh token으로 access token 재발급
 */
export type RefreshTokenResponse = ApiResponse<string | null>;



// WorkspaceController API Type
/**
 * /api/workspaces - workspace 생성
 */
export type CreateWorkspaceResponse = ApiResponse<WorkSpaceDTO | null>;

