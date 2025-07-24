export interface ApiResponse<T> {
    success: boolean;
    data?: T;
    message?: string;
    error?: string;
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
