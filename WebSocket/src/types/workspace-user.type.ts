/**
 * @fileoverview 워크스페이스 사용자 DTO 타입 (WebSocket용)
 *
 * Spring Boot WorkSpaceUserDTO와 1:1 매핑
 * - workspaceId: 워크스페이스 ID (UUID)
 * - userEmailId: 사용자 이메일 ID
 * - workspaceRole: 역할(OWNER, ADMIN, MEMBER, GUEST)
 * - membershipStatus: 멤버십 상태(ACTIVE, INACTIVE 등)
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

import { WorkspaceRole } from '../constants/workspace-role.enum';

export type WorkspaceUser = {
  workspaceId: string; // UUID
  userEmailId: string;
  workspaceRole: WorkspaceRole;
  membershipStatus: string; // 예: 'ACTIVE', 'INACTIVE', ...
};
