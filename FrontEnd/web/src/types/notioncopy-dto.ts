// NotionCopyDTO 폴더 하의 모든 DTO 타입 예시
export interface WorkSpaceUserDTO {
  workspaceId: string;
  userEmailId: string;
  workspaceRole: string;
  membershipStatus: string;
}

export interface WorkSpaceDTO {
  id: string;
  name: string;
  rootPageDTOS: PageDTO[];
}

export interface PageDTO {
  id: string;
  title: string;
  path: string;
  orderIndex: number;
  isRoot: boolean;
  workspaceId: string;
  workspaceName: string;
  parentPageId?: string;
  childPages?: PageDTO[];
}

export interface BlockDTO {
  id: string;
  pageId: string;
  blockType: string;
  content: string;
  properties?: Record<string, any>;
  parentId?: string;
  orderIndex?: number;
  childrenIds?: string[];
  hasChildren?: boolean;
}
