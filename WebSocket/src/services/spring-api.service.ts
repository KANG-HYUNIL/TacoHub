/**
 * @fileoverview Spring API 서버 연동 서비스
 *
 * 이 파일은 WebSocket 서버에서 외부 Spring Boot API 서버와의 HTTP 통신을 담당합니다.
 * - 워크스페이스/페이지/사용자 권한 검증 등 REST API 호출
 * - axios 기반 비동기 HTTP 요청 처리
 * - API 서버 주소 및 엔드포인트 관리
 *
 * @author TacoHub Team
 * @version 1.0.0
 */


import axios from 'axios';
import { applicationLogger } from '../utils/logger';
import { SpringApi } from '../constants/spring-api.enum';
import { WorkspaceUser } from '../types/workspace-user.type';
import { WorkspaceRole } from '../constants/workspace-role.enum';




/**
 * Spring Boot API 서버 base URL
 * (실제 운영/개발 환경에 맞게 수정)
 */
const SPRING_API_BASE_URL = process.env.PROXY_SERVER_URL || 'http://localhost:8080';

/**
 * 워크스페이스 내 사용자 역할 조회 (WorkSpaceUserDTO 반환)
 * @param workspaceId 워크스페이스 ID (UUID)
 * @param userId 사용자 ID (이메일 등)
 * @returns WorkspaceUser 타입 (Spring WorkSpaceUserDTO와 매핑)
 */
export async function fetchUserRoleInWorkspace(workspaceId: string, userId: string): Promise<WorkspaceUser | null> {
  try {
    // API 경로 치환
    const endpoint = SpringApi.FETCH_USER_ROLE_IN_WORKSPACE
      .replace('{workspaceId}', workspaceId)
      .replace('{userId}', userId);
    const url = `${SPRING_API_BASE_URL}${endpoint}`;
    const res = await axios.get(url);

    // Spring API는 ApiResponse<WorkSpaceUserDTO> 구조로 반환
    if (res.data && res.data.data) 
      {
      return res.data.data as WorkspaceUser;
    }
    return null;
  } catch (error) {
    applicationLogger.error('fetchUserRoleInWorkspace error:', error);
    return null;
  }
}

/**
 * 워크스페이스 내 사용자 역할(문자열)만 반환
 * @param workspaceId 워크스페이스 ID
 * @param userId 사용자 ID
 * @returns WorkspaceRole(enum) | null
 */
export async function fetchWorkspaceRole(workspaceId: string, userId: string): Promise<WorkspaceRole | null> {
  const user = await fetchUserRoleInWorkspace(workspaceId, userId);

  if (user && user.workspaceRole) 
    {
    return user.workspaceRole as WorkspaceRole;
  }
  return null;
}








