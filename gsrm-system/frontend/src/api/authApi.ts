import api from './axios';
import type { ApiResponse, LoginRequest, LoginResponse } from '../types';

/**
 * 認證相關 API.
 */
export const authApi = {
  /**
   * 使用者登入.
   */
  login: (data: LoginRequest) =>
    api.post<ApiResponse<LoginResponse>>('/auth/login', data),

  /**
   * 使用者登出.
   */
  logout: () =>
    api.post<ApiResponse<void>>('/auth/logout'),

  /**
   * 刷新 Token.
   */
  refreshToken: (refreshToken: string) =>
    api.post<ApiResponse<LoginResponse>>('/auth/refresh', { refreshToken }),
};
