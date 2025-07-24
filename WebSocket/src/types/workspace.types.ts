export interface Workspace {
    id: string;
    name: string;
    ownerId: string;
    members: WorkspaceMember[];
    createdAt: Date;
    updatedAt: Date;
}

export interface WorkspaceMember {
    userId: string;
    role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'GUEST';
    joinedAt: Date;
}

export interface Page {
    id: string;
    workspaceId: string;
    title: string;
    content: any;
    createdBy: string;
    createdAt: Date;
    updatedAt: Date;
}
