// 기준: 2025-07-25 NotionCopy Message Spec v1.0
export interface BlockDTO {
  id: string; // UUID
  pageId: string; // UUID
  blockType: string;
  content?: string;
  properties?: Record<string, any>;
  parentId?: string | null;
  orderIndex?: number;
  childrenIds?: string[];
  hasChildren?: boolean;
  metadata?: Record<string, any>;
  createdAt?: string; // ISO string
  updatedAt?: string; // ISO string
  createdBy?: string;
  lastEditedBy?: string;
}
