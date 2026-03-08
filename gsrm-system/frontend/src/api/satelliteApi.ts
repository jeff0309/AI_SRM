import api from './axios';
import type {
  ApiResponse, Page, Satellite, SatelliteCreateRequest, FrequencyBand,
} from '../types';

/**
 * 衛星相關 API.
 */
export const satelliteApi = {
  /** 取得所有衛星（分頁） */
  getAll: (page = 0, size = 20) =>
    api.get<ApiResponse<Page<Satellite>>>('/satellites', { params: { page, size } }),

  /** 取得已啟用衛星 */
  getEnabled: () =>
    api.get<ApiResponse<Satellite[]>>('/satellites/enabled'),

  /** 取得緊急任務衛星 */
  getEmergency: () =>
    api.get<ApiResponse<Satellite[]>>('/satellites/emergency'),

  /** 取得所有公司名稱 */
  getCompanies: () =>
    api.get<ApiResponse<string[]>>('/satellites/companies'),

  /** 依 ID 取得衛星 */
  getById: (id: number) =>
    api.get<ApiResponse<Satellite>>(`/satellites/${id}`),

  /** 依頻段取得衛星 */
  getByBand: (band: FrequencyBand) =>
    api.get<ApiResponse<Satellite[]>>(`/satellites/by-band/${band}`),

  /** 建立衛星 */
  create: (data: SatelliteCreateRequest) =>
    api.post<ApiResponse<Satellite>>('/satellites', data),

  /** 更新衛星 */
  update: (id: number, data: SatelliteCreateRequest) =>
    api.put<ApiResponse<Satellite>>(`/satellites/${id}`, data),

  /** 刪除衛星 */
  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/satellites/${id}`),

  /** 啟用/停用衛星 */
  setEnabled: (id: number, enabled: boolean) =>
    api.patch<ApiResponse<Satellite>>(`/satellites/${id}/enabled`, null, {
      params: { enabled },
    }),

  /** 設定緊急任務 */
  setEmergency: (id: number, isEmergency: boolean) =>
    api.patch<ApiResponse<Satellite>>(`/satellites/${id}/emergency`, null, {
      params: { isEmergency },
    }),
};
