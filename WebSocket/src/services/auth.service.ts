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
import { applicationLogger, withAuditLog } from '../utils/logger';

const JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET || 'default_access_secret';

/**
 * JWT Access Token 유효성 검증 함수
 */
export function isValidAccessToken(tokne : string) : boolean
{
    try {

        const payload = jwt.verify(tokne, JWT_ACCESS_SECRET);
        applicationLogger.info('Access Token is valid:', payload);
        return true; // 유효한 토큰

    } catch (error)
    {
        applicationLogger.error('Access Token validation failed:', error);
        return false; // 유효하지 않은 토큰
    }


}

/**
 * Jwt Access Token 만료 여부 검증 함수
 */
export function isExpiredAccessToken(token: string): boolean 
{
    try 
    {
        const decoded = jwt.decode(token);
        if (!decoded || typeof decoded === 'string') {
            applicationLogger.error('Invalid token format');
            return true; // 만료된 토큰
        }

        const exp = decoded.exp;
        if (!exp) {
            applicationLogger.error('Token does not contain expiration time');
            return true; // 만료된 토큰
        }

        const isValid = Date.now() < exp * 1000;
        applicationLogger.info(`Access Token is ${isValid ? 'valid' : 'expired'}`);
        return !isValid; // 만료된 토큰 여부 반환
    } 
    catch (error)
    {
        applicationLogger.error('Access Token expiration check failed:', error);
        return true; // 만료된 토큰
    }
}




/**
 * JWT Access Token 사용자 정보 추출 함수
 */



