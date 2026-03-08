import api from './axios';
import type {
  ApiResponse, Page, GroundStation, GroundStationRequest,
  StationUnavailability, FrequencyBand,
} from '../types';

/**
 * 地面站相關 API.
 */
export const groundStationApi = {
  /** 取得所有地面站（分頁） */
  getAll: (page = 0, size = 20) =>
    api.get<ApiResponse<Page<GroundStation>>>('/ground-stations', { params: { page, size } }),

  /** 取得已啟用地面站 */
  getEnabled: () =>
    api.get<ApiResponse<GroundStation[]>>('/ground-stations/enabled'),

  /** 依 ID 取得地面站 */
  getById: (id: number) =>
    api.get<ApiResponse<GroundStation>>(`/ground-stations/${id}`),

  /** 依頻段取得地面站 */
  getByBand: (band: FrequencyBand) =>
    api.get<ApiResponse<GroundStation[]>>(`/ground-stations/by-band/${band}`),

  /** 建立地面站 */
  create: (data: GroundStationRequest) =>
    api.post<ApiResponse<GroundStation>>('/ground-stations', data),

  /** 更新地面站 */
  update: (id: number, data: GroundStationRequest) =>
    api.put<ApiResponse<GroundStation>>(`/ground-stations/${id}`, data),

  /** 刪除地面站 */
  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/ground-stations/${id}`),

  /** 啟用/停用地面站 */
  setEnabled: (id: number, enabled: boolean) =>
    api.patch<ApiResponse<GroundStation>>(`/ground-stations/${id}/enabled`, null, {
      params: { enabled },
    }),

  /** 取得維護時段 */
  getUnavailabilities: (id: number, startTime?: string, endTime?: string) =>
    api.get<ApiResponse<StationUnavailability[]>>(
      `/history/ground-stations/${id}/unavailabilities`,
      { params: { startTime, endTime } }
    ),
};
