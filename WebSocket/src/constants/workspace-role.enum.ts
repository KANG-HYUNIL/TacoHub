/**
 * @fileoverview 워크스페이스 역할(권한) Enum (WebSocket용)
 *
 * Spring Boot WorkSpaceRole Enum과 1:1 매핑
 * - OWNER: 모든 권한
 * - ADMIN: 관리/초대/삭제/편집/조회
 * - MEMBER: 편집/조회/삭제
 * - GUEST: 조회만 가능
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

export enum WorkspaceRole {
  OWNER = 'OWNER',
  ADMIN = 'ADMIN',
  MEMBER = 'MEMBER',
  GUEST = 'GUEST',
}

// 역할별 권한 매핑 (enum + 권한 플래그)
export const WorkspaceRolePermissions = {
  OWNER: {
    canManageWorkspace: true,
    canInviteAndDeleteUsers: true,
    canDeletePage: true,
    canEditPage: true,
    canViewPage: true,
  },
  ADMIN: {
    canManageWorkspace: true,
    canInviteAndDeleteUsers: true,
    canDeletePage: true,
    canEditPage: true,
    canViewPage: true,
  },
  MEMBER: {
    canManageWorkspace: false,
    canInviteAndDeleteUsers: false,
    canDeletePage: true,
    canEditPage: true,
    canViewPage: true,
  },
  GUEST: {
    canManageWorkspace: false,
    canInviteAndDeleteUsers: false,
    canDeletePage: false,
    canEditPage: false,
    canViewPage: true,
  },
} as const;
