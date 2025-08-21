import { PageDTO, BlockDTO } from '../types/notioncopy-dto';
import { GetPageResponse, GetPageBlocksResponse, UpdatePageTitleResponse, UpdatePageTitleRequest } from '../types/api';
import { axiosWithAccessToken } from '../utils/authUtils';

/**
 * 페이지 관련 API 서비스
 */
export class PageService {
  
  /**
   * 페이지 정보 조회
   * @param pageId 조회할 페이지 ID
   * @returns Promise<PageDTO> 페이지 정보
   */
  static async getPage(pageId: string): Promise<PageDTO> {
    const response = await axiosWithAccessToken<GetPageResponse>(
      `/api/pages/${pageId}`,
      undefined,
      'get',
      isGetPageResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const pageData = response.apiResponse.data as PageDTO;
      if (pageData) {
        return pageData;
      }
    }

    throw new Error('페이지 조회에 실패했습니다.');
  }

  /**
   * 페이지의 모든 블록 조회
   * @param pageId 조회할 페이지 ID
   * @returns Promise<BlockDTO[]> 블록 목록
   */
  static async getPageBlocks(pageId: string): Promise<BlockDTO[]> {
    const response = await axiosWithAccessToken<GetPageBlocksResponse>(
      `/api/pages/${pageId}/blocks`,
      undefined,
      'get',
      isGetPageBlocksResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const blocksData = response.apiResponse.data as BlockDTO[];
      if (blocksData) {
        return blocksData;
      }
    }

    return []; // 블록이 없는 경우 빈 배열 반환
  }

  /**
   * 페이지 제목 수정
   * @param pageId 수정할 페이지 ID
   * @param title 새로운 제목
   * @returns Promise<PageDTO> 수정된 페이지 정보
   */
  static async updatePageTitle(pageId: string, title: string): Promise<PageDTO> {
    const response = await axiosWithAccessToken<UpdatePageTitleResponse, string>(
      `/api/pages/${pageId}/title`,
      title,
      'put',
      isUpdatePageTitleResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const pageData = response.apiResponse.data as PageDTO;
      if (pageData) {
        return pageData;
      }
    }

    throw new Error('페이지 제목 수정에 실패했습니다.');
  }
}

/**
 * GetPageResponse 타입 가드 함수
 */
function isGetPageResponse(res: any): res is GetPageResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

/**
 * GetPageBlocksResponse 타입 가드 함수
 */
function isGetPageBlocksResponse(res: any): res is GetPageBlocksResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

/**
 * UpdatePageTitleResponse 타입 가드 함수
 */
function isUpdatePageTitleResponse(res: any): res is UpdatePageTitleResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

export default PageService;
