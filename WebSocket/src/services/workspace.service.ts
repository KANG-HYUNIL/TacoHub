/**
 * @fileoverview 워크스페이스 관련 백엔드 API 연동 서비스
 * 
 * 이 파일은 Spring Boot 백엔드와 연동하여 워크스페이스 관련 데이터를 처리합니다:
 * 1. 워크스페이스 정보 조회 및 검증
 * 2. 사용자 워크스페이스 접근 권한 확인
 * 3. 워크스페이스 멤버십 관리
 * 4. 실시간 협업을 위한 데이터 동기화
 * 
 * WebSocket 서버와 Spring Boot 백엔드 간의 데이터 일관성을 보장하여
 * 안전하고 신뢰할 수 있는 실시간 협업 환경을 제공합니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import axios from 'axios';
import { logger } from '../utils/logger';

/**
 * 워크스페이스 정보 인터페이스
 * Spring Boot API에서 반환하는 워크스페이스 데이터 구조
 * 
 * @interface WorkspaceInfo
 * @property {string} id - 워크스페이스 고유 식별자
 * @property {string} name - 워크스페이스 이름
 * @property {string} userRole - 현재 사용자의 워크스페이스 내 역할 (OWNER, MEMBER, GUEST)
 */
export interface WorkspaceInfo {
    id: string;
    name: string;
    userRole: string;
}

/**
 * 워크스페이스 관련 백엔드 API 연동 서비스 클래스
 * 
 * @class WorkspaceService
 * 
 * 주요 기능:
 * - Spring Boot 백엔드와의 HTTP API 통신
 * - 워크스페이스 데이터 조회 및 검증
 * - 사용자 권한 확인 및 접근 제어
 * - 에러 처리 및 로깅
 */
export class WorkspaceService {
    private readonly springBootUrl: string;
    
    /**
     * WorkspaceService 생성자
     * 환경변수에서 Spring Boot 서버 URL을 로드합니다
     * 
     * @constructor
     */
    constructor() {
        this.springBootUrl = process.env.SPRING_BOOT_URL || 'http://localhost:8080';
        logger.info(`WorkspaceService initialized with Spring Boot URL: ${this.springBootUrl}`);
    }
    
    /**
     * 워크스페이스 정보 조회
     * 
     * @method getWorkspaceInfo
     * @param {string} workspaceId - 조회할 워크스페이스 ID
     * @param {string} token - 인증 JWT 토큰
     * @returns {Promise<WorkspaceInfo | null>} 워크스페이스 정보 또는 null (실패 시)
     * 
     * API 호출:
     * - GET /api/workspaces/{workspaceId}
     * - Authorization: Bearer {token}
     * 
     * 반환 데이터:
     * - 워크스페이스 기본 정보
     * - 사용자의 워크스페이스 내 역할
     * - 접근 권한 정보
     */
    async getWorkspaceInfo(workspaceId: string, token: string): Promise<WorkspaceInfo | null> {
        try {
            logger.debug(`Fetching workspace info for: ${workspaceId}`);
            
            const response = await axios.get(`${this.springBootUrl}/api/workspaces/${workspaceId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                timeout: 5000 // 5초 타임아웃
            });
            
            const workspaceInfo: WorkspaceInfo = response.data;
            logger.debug(`Workspace info retrieved: ${workspaceInfo.name} (Role: ${workspaceInfo.userRole})`);
            
            return workspaceInfo;
            
        } catch (error) {
            // HTTP 오류 상세 로깅
            if (axios.isAxiosError(error)) {
                const status = error.response?.status;
                const statusText = error.response?.statusText;
                
                if (status === 404) {
                    logger.warn(`Workspace not found: ${workspaceId}`);
                } else if (status === 403) {
                    logger.warn(`Access denied to workspace: ${workspaceId}`);
                } else {
                    logger.error(`HTTP error fetching workspace info: ${status} ${statusText}`);
                }
            } else {
                logger.error('Failed to get workspace info:', error);
            }
            
            return null;
        }
    }
    
    /**
     * 사용자의 워크스페이스 접근 권한 검증
     * 
     * @method validateUserAccess
     * @param {string} workspaceId - 접근할 워크스페이스 ID
     * @param {string} userId - 사용자 ID
     * @param {string} token - 인증 JWT 토큰
     * @returns {Promise<boolean>} 접근 권한 여부
     * 
     * API 호출:
     * - GET /api/workspaces/{workspaceId}/users/{userId}/access
     * - Authorization: Bearer {token}
     * 
     * 검증 내용:
     * - 워크스페이스 존재 여부
     * - 사용자 멤버십 상태
     * - 접근 권한 레벨 확인
     */
    async validateUserAccess(workspaceId: string, userId: string, token: string): Promise<boolean> {
        try {
            logger.debug(`Validating user access: ${userId} to workspace ${workspaceId}`);
            
            const response = await axios.get(
                `${this.springBootUrl}/api/workspaces/${workspaceId}/users/${userId}/access`, 
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: 5000 // 5초 타임아웃
                }
            );
            
            const hasAccess = response.data.hasAccess;
            logger.debug(`User access validation result: ${hasAccess ? 'GRANTED' : 'DENIED'}`);
            
            return hasAccess;
            
        } catch (error) {
            // 접근 권한 검증 실패 시 보수적으로 false 반환
            if (axios.isAxiosError(error)) {
                const status = error.response?.status;
                
                if (status === 404) {
                    logger.warn(`Workspace or user not found during access validation`);
                } else if (status === 403) {
                    logger.warn(`Access denied during validation: ${userId} to ${workspaceId}`);
                } else {
                    logger.error(`HTTP error during access validation: ${status}`);
                }
            } else {
                logger.error('Failed to validate user access:', error);
            }
            
            return false; // 오류 시 안전하게 접근 거부
        }
    }
    
    /**
     * 워크스페이스 멤버 목록 조회 (향후 확장용)
     * 
     * @method getWorkspaceMembers
     * @param {string} workspaceId - 워크스페이스 ID
     * @param {string} token - 인증 토큰
     * @returns {Promise<any[]>} 멤버 목록
     */
    async getWorkspaceMembers(workspaceId: string, token: string): Promise<any[]> {
        try {
            const response = await axios.get(
                `${this.springBootUrl}/api/workspaces/${workspaceId}/members`,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );
            
            return response.data || [];
            
        } catch (error) {
            logger.error('Failed to get workspace members:', error);
            return [];
        }
    }
}

/**
 * WorkspaceService 싱글톤 인스턴스
 * 애플리케이션 전체에서 재사용되는 워크스페이스 서비스 인스턴스
 */
export const workspaceService = new WorkspaceService();
