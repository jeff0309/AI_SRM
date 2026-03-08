import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { mockLoginResponse, mockGroundStations, mockSatellites, mockSessions, mockGanttData, wrapResponse } from './mockData';

const isMock = window.location.hostname.includes('github.io') || window.location.search.includes('mock=true');
const BASE_PATH = isMock ? '/AI_SRM/' : '/';

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

// 1. Mock 請求攔截器 (偵測到特定 URL 就回傳模擬資料)
if (isMock) {
  api.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    await new Promise(resolve => setTimeout(resolve, 500)); // 模擬網路延遲

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
      // 透過 reject 傳遞模擬回應，稍後在回應攔截器中轉為成功
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
}

// 2. JWT Token 請求攔截器
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

// 3. 綜合回應攔截器 (處理 Mock 成功 & 401 自動登出)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 處理 Mock 模式的回傳 (模擬成功)
    if (isMock && error.response && error.response.status === 200) {
      return error.response;
    }

    // 處理 401 未授權 (跳轉登入)
    if (error.response?.status === 401) {
      localStorage.removeItem('gsrm_token');
      localStorage.removeItem('gsrm_user');
      window.location.href = `${BASE_PATH}login`;
    }
    return Promise.reject(error);
  }
);

export default api;
