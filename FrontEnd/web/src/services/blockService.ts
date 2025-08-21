import { BlockDTO } from '../types/notioncopy-dto';
import { CreateBlockResponse, GetBlockResponse, UpdateBlockResponse, DeleteBlockResponse } from '../types/api';
import { axiosWithAccessToken } from '../utils/authUtils';

/**
 * 블록 관련 API 서비스
 */
export class BlockService {
  
  /**
   * 블록 생성
   * @param blockDTO 생성할 블록 정보
   * @returns Promise<BlockDTO> 생성된 블록 정보
   */
  static async createBlock(blockDTO: BlockDTO): Promise<BlockDTO> {
    const response = await axiosWithAccessToken<CreateBlockResponse, BlockDTO>(
      '/api/blocks',
      blockDTO,
      'post',
      isCreateBlockResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const blockData = response.apiResponse.data as BlockDTO;
      if (blockData) {
        return blockData;
      }
    }

    throw new Error('블록 생성에 실패했습니다.');
  }

  /**
   * 블록 조회
   * @param blockId 조회할 블록 ID
   * @returns Promise<BlockDTO> 블록 정보
   */
  static async getBlock(blockId: string): Promise<BlockDTO> {
    const response = await axiosWithAccessToken<GetBlockResponse>(
      `/api/blocks/${blockId}`,
      undefined,
      'get',
      isGetBlockResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const blockData = response.apiResponse.data as BlockDTO;
      if (blockData) {
        return blockData;
      }
    }

    throw new Error('블록 조회에 실패했습니다.');
  }

  /**
   * 블록 수정
   * @param blockDTO 수정할 블록 정보
   * @returns Promise<BlockDTO> 수정된 블록 정보
   */
  static async updateBlock(blockDTO: BlockDTO): Promise<BlockDTO> {
    const response = await axiosWithAccessToken<UpdateBlockResponse, BlockDTO>(
      `/api/blocks/${blockDTO.id}`,
      blockDTO,
      'put',
      isUpdateBlockResponse
    );

    if (response.success && response.apiResponse && 'data' in response.apiResponse) {
      const blockData = response.apiResponse.data as BlockDTO;
      if (blockData) {
        return blockData;
      }
    }

    throw new Error('블록 수정에 실패했습니다.');
  }

  /**
   * 블록 삭제
   * @param blockId 삭제할 블록 ID
   * @returns Promise<void>
   */
  static async deleteBlock(blockId: string): Promise<void> {
    const response = await axiosWithAccessToken<DeleteBlockResponse>(
      `/api/blocks/${blockId}`,
      undefined,
      'delete',
      isDeleteBlockResponse
    );

    if (!response.success) {
      throw new Error('블록 삭제에 실패했습니다.');
    }
  }
}

/**
 * CreateBlockResponse 타입 가드 함수
 */
function isCreateBlockResponse(res: any): res is CreateBlockResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

/**
 * GetBlockResponse 타입 가드 함수
 */
function isGetBlockResponse(res: any): res is GetBlockResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

/**
 * UpdateBlockResponse 타입 가드 함수
 */
function isUpdateBlockResponse(res: any): res is UpdateBlockResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

/**
 * DeleteBlockResponse 타입 가드 함수
 */
function isDeleteBlockResponse(res: any): res is DeleteBlockResponse {
  return res && typeof res.success === 'boolean' && res.success === true;
}

export default BlockService;
