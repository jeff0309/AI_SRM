import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { mockLoginResponse, mockGroundStations, mockSatellites, mockSessions, mockGanttData, wrapResponse } from './mockData';

const isMock = window.location.hostname.includes('github.io') || window.location.search.includes('mock=true');

/**
 * 建立 Axios 實例並設定基礎 URL 與攔截器.
 */
const api = axios.create({
  baseURL: isMock ? undefined : '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

if (isMock) {
  api.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500));

    const url = config.url || '';
    let mockResult: any = null;

    if (url.includes('/auth/login')) mockResult = mockLoginResponse;
    else if (url.includes('/ground-stations/enabled')) mockResult = mockGroundStations;
    else if (url.includes('/satellites/enabled')) mockResult = mockSatellites;
    else if (url.includes('/schedule/sessions')) {
      if (url.match(/\/schedule\/sessions\/\d+\/gantt/)) mockResult = mockGanttData;
      else mockResult = mockSessions;
    }
    else if (url.includes('/schedule/strategies')) mockResult = ['Proportional', 'NoShortening'];

    if (mockResult) {
      return Promise.reject({
        config,
        response: {
          data: wrapResponse(mockResult),
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
        } as AxiosResponse,
      });
    }
    return config;
  });

  // Intercept the rejection and return it as a success if it's our mock
  api.interceptors.response.use(
    (response: AxiosResponse) => response,
    (error: any) => {
      if (error.response && error.response.status === 200) {
        return error.response;
      }
      return Promise.reject(error);
    }
  );
}

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
