import { API_ENDPOINTS } from '../../constants/api';
import { axiosWithAccessToken } from '../authUtils';
import type { WorkSpaceDTO } from '../../types/notioncopy-dto';
import type { ApiResponse, ErrorResponse } from '../../types/api';

/**
 * 현재 사용자가 속한 모든 워크스페이스 목록을 조회하는 API 함수
 * @returns 성공 시 워크스페이스 목록, 실패 시 빈 배열
 */
export async function getUserWorkspaces(): Promise<WorkSpaceDTO[]> {
    /**
     * 1. axiosWithAccessToken을 사용하여 API 호출
     * 2. 정상/비정상 응답 처리
     * 3. 성공 시 워크스페이스 목록 반환, 실패 시 빈 배열 반환
     */

    console.log('[getUserWorkspaces] 사용자 워크스페이스 목록 조회 시작');

    // 1. axiosWithAccessToken을 사용하여 API 호출 
    const result = await axiosWithAccessToken<ApiResponse<WorkSpaceDTO[]>>(
        API_ENDPOINTS.WORKSPACE_MY_LIST,
        undefined, // body data 없음 (GET 요청)
        'get',
        isGetUserWorkspacesResponse
    );

    // 2. 정상/비정상 응답 처리
    if (result.success && result.apiResponse) {
        const response = result.apiResponse as ApiResponse<WorkSpaceDTO[]>;
        console.log('[getUserWorkspaces] 워크스페이스 목록 조회 성공:', {
            count: response.data?.length || 0,
            workspaces: response.data
        });
        
        // 3. 성공 시 워크스페이스 목록 반환
        return response.data || [];
    } else {
        // 실패 시 에러 로깅 및 빈 배열 반환
        console.error('[getUserWorkspaces] 워크스페이스 목록 조회 실패:', {
            success: result.success,
            apiResponse: result.apiResponse,
            message: result.apiResponse?.message,
            errorCode: result.apiResponse?.errorCode
        });

        // 사용자에게 에러 알림 (선택적)
        if (result.apiResponse?.message) {
            console.warn('[getUserWorkspaces] 서버 에러 메시지:', result.apiResponse.message);
        }

        return [];
    }
}

/**
 * GetUserWorkspacesResponse Type Guard Method
 * @param res API 응답 객체
 * @returns boolean - ApiResponse<WorkSpaceDTO[]> 타입인지 여부
 */
function isGetUserWorkspacesResponse(
    res: ApiResponse<WorkSpaceDTO[]> | ErrorResponse
): res is ApiResponse<WorkSpaceDTO[]> {
    return (
        typeof res === 'object' &&
        res !== null &&
        typeof res.success === 'boolean' &&
        res.success === true &&
        Array.isArray(res.data) // data가 배열인지 확인
        // 배열 내부 요소의 구조는 런타임에서 검증하기 어려우므로 기본적인 배열 체크만 수행
    );
}
