// Socket 이벤트별 payload 타입 예시
export interface JoinWorkspacePayload {
  workspaceId: string;
}

export interface UpdatePagePayload {
  pageId: string;
  data: any; // Yjs 데이터 등
}

export interface ReceiveUpdatePayload {
  pageId: string;
  data: any;
}
