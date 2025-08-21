export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data?: T;
    timestamp?: string;
    errorCode?: string;
}

export interface AuthResponse {
    token: string;
    user: {
        id: string;
        email: string;
        name?: string;
    };
}

export interface WorkspaceApiResponse {
    id: string;
    name: string;
    userRole: string;
}
