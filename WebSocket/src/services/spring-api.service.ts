import axios from 'axios';
import { logger } from '../utils/logger';

export class SpringApiService {
    private readonly baseUrl: string;
    
    constructor() {
        this.baseUrl = process.env.SPRING_BOOT_URL || 'http://localhost:8080';
    }
    
    async makeAuthenticatedRequest(endpoint: string, token: string, method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET', data?: any) {
        try {
            const response = await axios({
                method,
                url: `${this.baseUrl}${endpoint}`,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                data
            });
            
            return response.data;
        } catch (error) {
            logger.error(`Spring Boot API request failed for ${endpoint}:`, error);
            throw error;
        }
    }
    
    async validateToken(token: string): Promise<boolean> {
        try {
            await this.makeAuthenticatedRequest('/api/auth/validate', token);
            return true;
        } catch (error) {
            return false;
        }
    }
}

export const springApiService = new SpringApiService();
