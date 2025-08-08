
import axios from 'axios';
import { API_ENDPOINTS } from '../../constants/api';
import type { CreateWorkspaceResponse, ErrorResponse } from '../../types/api';
import { axiosWithAccessToken, getAccessToken } from '../authUtils';


/**
 * Workspace 생성 요청 API 호출 함수
 * @param workspaceName Workspace 이름 
 * @returns 성공 시 true, 실패 시 false
 */ 
export async function createWorkspace(
    workspaceName : string 
) : Promise<boolean>
{
  /**\
   * 1. Workspace 생성 요청 DTO 생성
   * 2. axios로 API 호출(Header에 JWT Access Token 포함)
   * 3. 정상/비정상 응답 처리
   * 4. 성공 시 true 반환, 실패 시 false 반환
   */

    // 1. Workspace 생성 요청 DTO 생성
    const createWorkspaceDto  = {
        name : workspaceName
    }; 

    // 2. axios로 API 호출 (Header에 JWT Access Token 포함)
    const apiUrl : string = API_ENDPOINTS.WORKSPACE_CREATE;

    // 3. 요청 전달 및 응답 받기
    const result = await axiosWithAccessToken<CreateWorkspaceResponse>(
        apiUrl, 
        createWorkspaceDto,
        'post',
        isCreateWorkspaceResponse
    );
    
    // 4. 정상/비정상 응답 처리

    if (result.success) {
        // 정상 응답 처리
        alert(`Workspace "${workspaceName}" created successfully!`);
        return true; // Workspace 생성 성공
    } else {
        // 비정상 응답 처리
        console.log('[워크스페이스 생성 실패]', {
            apiResponse: result.apiResponse,
            message: result.apiResponse?.message,
            errorCode: result.apiResponse?.errorCode,
            error: (result.apiResponse as any)?.data?.error,
            details: (result.apiResponse as any)?.data?.details,
        });
        alert(`Workspace creation failed: ${result.apiResponse?.message || 'Unknown error message'}\n- ${result.apiResponse?.errorCode || 'Unknown error code'}`);
        return false; // Workspace 생성 실패
    }


}


/**
 * CreateWorkspaceResponse Type Guard Method
 * @param res 
 * @returns boolean CreateWorkspaceResponse인지 여부
 */
export function isCreateWorkspaceResponse(
  res: CreateWorkspaceResponse | ErrorResponse
): res is CreateWorkspaceResponse {
  return (
    typeof res === 'object' &&
    res !== null &&
    typeof res.success === 'boolean' &&
    res.success === true &&
    typeof res.data === 'object' &&
    res.data !== null &&
    typeof (res.data as any).id === 'string' && // 혹은 UUID 패턴 체크
    typeof (res.data as any).name === 'string'
    // 필요하다면 rootPageDTOS 등 추가 체크
  );
}


