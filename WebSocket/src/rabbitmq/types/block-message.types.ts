// 기준: 2025-07-25 NotionCopy Message Spec v1.0
import { BaseMessage } from './base-message.types';
import { BlockOperation } from './block-operation.enum';
import { BlockDTO } from '../../types/notionCopy/block-dto.types'

export interface BlockMessage extends BaseMessage {
  blockOperation: BlockOperation;
  blockDTO: BlockDTO;
  workspaceId: string;
  userId: string;
}
