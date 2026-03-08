import api from './axios';
import type { ApiResponse } from '../types';

/**
 * 匯入/匯出相關 API.
 */
export const importApi = {
  /** 取得支援的匯入格式 */
  getSupportedFormats: () =>
    api.get<ApiResponse<string[]>>('/import/formats'),

  /** 匯入衛星需求 */
  importSatelliteRequests: (file: File, sessionId: number) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ApiResponse<unknown[]>>('/import/satellite-requests', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      params: { sessionId },
    });
  },

  /** 匯入地面站維護時段 */
  importStationUnavailabilities: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ApiResponse<unknown[]>>('/import/station-unavailabilities', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const exportApi = {
  /** 取得支援的匯出格式 */
  getSupportedFormats: () =>
    api.get<ApiResponse<string[]>>('/export/formats'),

  /** 下載 XML 匯出 */
  downloadXml: (sessionId: number) =>
    api.get(`/export/sessions/${sessionId}/xml`, { responseType: 'blob' }),

  /** 下載 CSV 匯出 */
  downloadCsv: (sessionId: number) =>
    api.get(`/export/sessions/${sessionId}/csv`, { responseType: 'blob' }),
};

/**
 * 觸發瀏覽器下載 Blob.
 */
export function triggerDownload(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a   = document.createElement('a');
  a.href     = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
