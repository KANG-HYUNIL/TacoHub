/**
 * @fileoverview Spring API 서버 엔드포인트 Enum
 *
 * Spring Boot REST API 경로를 상수로 관리
 * - 환경변수 대신 코드에서 직접 관리
 *
 * @author TacoHub Team
 * @version 1.0.0
 */

export enum SpringApi {
  // 워크스페이스 내 사용자 역할 조회
  FETCH_USER_ROLE_IN_WORKSPACE = '/api/workspaces/{workspaceId}/users/{userId}/role',
  // ... (추후 다른 API 경로 추가)
}
