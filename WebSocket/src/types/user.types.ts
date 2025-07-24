export interface User {
    id: string;
    email: string;
    name?: string;
    workspaces: string[];
}

export interface UserSession {
    userId: string;
    socketId: string;
    workspaceId?: string;
    pageId?: string;
    connectedAt: Date;
}
