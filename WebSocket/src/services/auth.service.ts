/**
 * @fileoverview JWT 기반 인증 서비스
 * 
 * 이 파일은 WebSocket 연결 및 API 요청에 대한 JWT 토큰 기반 인증을 담당합니다:
 * 1. JWT 토큰 검증 및 디코딩
 * 2. 토큰 생성 및 갱신
 * 3. 사용자 인증 상태 관리
 * 4. Spring Boot 백엔드와의 인증 정보 동기화
 * 
 * TacoHub의 모든 실시간 통신에서 보안을 보장하는 핵심 서비스입니다.
 * 
 * @author TacoHub Team
 * @version 1.0.0
 */

import jwt from 'jsonwebtoken';
import { logger } from '../utils/logger';

/**
 * JWT 토큰 페이로드 인터페이스
 * Spring Boot 백엔드에서 생성하는 JWT 토큰의 구조를 정의합니다
 * 
 * @interface AuthTokenPayload
 * @property {string} userId - 사용자 고유 식별자
 * @property {string} email - 사용자 이메일 주소
 * @property {string} [workspaceId] - 현재 작업 중인 워크스페이스 ID (선택적)
 */
export interface AuthTokenPayload {
    userId: string;
    email: string;
    workspaceId?: string;
}

/**
 * JWT 기반 인증 서비스 클래스
 * 
 * @class AuthService
 * 
 * 주요 기능:
 * - JWT 토큰 검증 및 파싱
 * - 토큰 생성 (개발/테스트용)
 * - 인증 실패 처리 및 로깅
 * - Spring Boot와의 토큰 호환성 보장
 */
export class AuthService {
    private readonly jwtSecret: string;
    
    /**
     * AuthService 생성자
     * 환경변수에서 JWT 시크릿 키를 로드합니다
     * 
     * @constructor
     */
    constructor() {
        this.jwtSecret = process.env.JWT_SECRET || 'default-secret';
        
        // 프로덕션 환경에서 기본 시크릿 사용 시 경고
        if (process.env.NODE_ENV === 'production' && this.jwtSecret === 'default-secret') {
            logger.warn('Using default JWT secret in production. Please set JWT_SECRET environment variable.');
        }
    }
    
    /**
     * JWT 토큰 검증 및 디코딩
     * 
     * @method verifyToken
     * @param {string} token - 검증할 JWT 토큰
     * @returns {AuthTokenPayload | null} 디코딩된 토큰 페이로드 또는 null (검증 실패 시)
     * 
     * 검증 과정:
     * 1. JWT 서명 검증
     * 2. 토큰 만료 시간 확인
     * 3. 페이로드 구조 검증
     * 4. 오류 발생 시 로깅 및 null 반환
     */
    verifyToken(token: string): AuthTokenPayload | null {
        try {
            // JWT 토큰 검증 및 디코딩
            const decoded = jwt.verify(token, this.jwtSecret) as AuthTokenPayload;
            
            // 필수 필드 존재 여부 확인
            if (!decoded.userId || !decoded.email) {
                logger.error('Invalid token payload: missing required fields');
                return null;
            }
            
            logger.debug(`Token verified successfully for user: ${decoded.userId}`);
            return decoded;
            
        } catch (error) {
            // JWT 검증 실패 시 상세 로깅
            if (error instanceof jwt.JsonWebTokenError) {
                logger.error('JWT verification failed:', error.message);
            } else if (error instanceof jwt.TokenExpiredError) {
                logger.warn('JWT token expired:', error.message);
            } else {
                logger.error('Unexpected error during token verification:', error);
            }
            
            return null;
        }
    }
    
    /**
     * JWT 토큰 생성
     * 주로 개발 및 테스트 환경에서 사용되며, 프로덕션에서는 Spring Boot가 토큰을 생성합니다
     * 
     * @method generateToken
     * @param {AuthTokenPayload} payload - 토큰에 포함할 사용자 정보
     * @returns {string} 생성된 JWT 토큰
     * 
     * 토큰 설정:
     * - 만료 시간: 24시간
     * - 서명 알고리즘: HS256 (기본값)
     * - 이슈어: TacoHub WebSocket Server
     */
    generateToken(payload: AuthTokenPayload): string {
        const tokenOptions: jwt.SignOptions = {
            expiresIn: '24h',
            issuer: 'TacoHub-WebSocket-Server'
        };
        
        const token = jwt.sign(payload, this.jwtSecret, tokenOptions);
        
        logger.debug(`Token generated for user: ${payload.userId}`);
        return token;
    }
    
    /**
     * 토큰에서 사용자 ID 추출 (빠른 접근용)
     * 
     * @method extractUserId
     * @param {string} token - JWT 토큰
     * @returns {string | null} 사용자 ID 또는 null
     */
    extractUserId(token: string): string | null {
        const payload = this.verifyToken(token);
        return payload ? payload.userId : null;
    }
    
    /**
     * 토큰 유효성 간단 확인 (불린 반환)
     * 
     * @method isTokenValid
     * @param {string} token - JWT 토큰
     * @returns {boolean} 토큰 유효성 여부
     */
    isTokenValid(token: string): boolean {
        return this.verifyToken(token) !== null;
    }
}

/**
 * AuthService 싱글톤 인스턴스
 * 애플리케이션 전체에서 재사용되는 인증 서비스 인스턴스
 */
export const authService = new AuthService();
