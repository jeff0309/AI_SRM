import api from './axios';
import type {
  ApiResponse, Page, ScheduleSession, ScheduleSessionRequest,
  GanttChartData, ScheduleResultResponse, ManualPassRequest,
} from '../types';

/**
 * 排程相關 API.
 */
export const scheduleApi = {
  /** 取得所有 Session（分頁） */
  getAllSessions: (page = 0, size = 20) =>
    api.get<ApiResponse<Page<ScheduleSession>>>('/schedule/sessions', { params: { page, size } }),

  /** 依 ID 取得 Session */
  getSessionById: (id: number) =>
    api.get<ApiResponse<ScheduleSession>>(`/schedule/sessions/${id}`),

  /** 建立 Session */
  createSession: (data: ScheduleSessionRequest) =>
    api.post<ApiResponse<ScheduleSession>>('/schedule/sessions', data),

  /** 更新 Session */
  updateSession: (id: number, data: ScheduleSessionRequest) =>
    api.put<ApiResponse<ScheduleSession>>(`/schedule/sessions/${id}`, data),

  /** 刪除 Session */
  deleteSession: (id: number) =>
    api.delete<ApiResponse<void>>(`/schedule/sessions/${id}`),

  /** 執行排程 */
  executeSchedule: (id: number, strategy?: string) =>
    api.post<ApiResponse<ScheduleResultResponse>>(
      `/schedule/sessions/${id}/execute`,
      null,
      { params: { strategy } }
    ),

  /** 重置排程 */
  resetSchedule: (id: number) =>
    api.post<ApiResponse<void>>(`/schedule/sessions/${id}/reset`),

  /** 取得甘特圖資料 */
  getGanttData: (id: number) =>
    api.get<ApiResponse<GanttChartData>>(`/schedule/sessions/${id}/gantt`),

  /** 取得 Pass 列表 */
  getPasses: (id: number) =>
    api.get<ApiResponse<unknown[]>>(`/schedule/sessions/${id}/passes`),

  /** 新增手動 Pass */
  addManualPass: (data: ManualPassRequest) =>
    api.post<ApiResponse<unknown>>('/schedule/passes/manual', data),

  /** 刪除 Pass */
  removePass: (passId: number) =>
    api.delete<ApiResponse<void>>(`/schedule/passes/${passId}`),

  /** 取得可用策略列表 */
  getAvailableStrategies: () =>
    api.get<ApiResponse<string[]>>('/schedule/strategies'),
};
