// 기준: 2025-07-25 NotionCopy Message Spec v1.0
import { MessageType } from './message-type.enum';

export interface BaseMessage {
  messageId: string; // UUID
  messageType: MessageType;
  timestamp: string; // ISO string
  metadata?: Record<string, any>;
  // getRoutingKey는 실제 객체에서 구현 (함수 타입은 제외)
}
