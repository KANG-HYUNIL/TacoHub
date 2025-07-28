// 백엔드 및 웹소켓 서버의 DTO 타입을 원본 구조 그대로 반영

// BaseDateDTO
export interface BaseDateDTO {
  createdAt: string;
  updatedAt: string;
}

// AccountDto
export interface AccountDto {
  emailId: string;
  password: string;
  name: string;
  role: string;
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

// BlockDTO (WebSocket 타입과 통일)
export interface BlockDTO {
  id: string;
  pageId: string;
  blockType: string;
  content?: string;
  properties?: Record<string, any>;
  parentId?: string | null;
  orderIndex?: number;
  childrenIds?: string[];
  hasChildren?: boolean;
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  lastEditedBy?: string;
}

// PageDTO (WebSocket 타입과 통일, 예시)
export interface PageDTO {
  id: string;
  title: string;
  path: string;
  orderIndex: number;
  workspaceId: string;
  parentId?: string | null;
  createdAt?: string;
  updatedAt?: string;
}
