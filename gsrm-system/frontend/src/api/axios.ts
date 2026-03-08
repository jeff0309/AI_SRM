import axios from 'axios';

/**
 * 建立 Axios 實例並設定基礎 URL 與攔截器.
 */
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// 請求攔截器：自動加入 JWT Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('gsrm_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 回應攔截器：處理 401 自動登出
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('gsrm_token');
      localStorage.removeItem('gsrm_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
