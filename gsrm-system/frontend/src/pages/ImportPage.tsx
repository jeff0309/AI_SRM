import React, { useRef, useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { scheduleApi } from '../api/scheduleApi';
import { importApi, exportApi, triggerDownload } from '../api/importExportApi';

// ─────────── Types ───────────

interface UploadState {
  status: 'idle' | 'uploading' | 'success' | 'error';
  message?: string;
  count?: number;
}

// ─────────── File Upload Zone ───────────

interface DropZoneProps {
  accept: string;
  label: string;
  hint: string;
  onFile: (file: File) => void;
  disabled?: boolean;
}

function DropZone({ accept, label, hint, onFile, disabled }: DropZoneProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    if (disabled) return;
    const file = e.dataTransfer.files[0];
    if (file) onFile(file);
  }, [disabled, onFile]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) onFile(file);
    // Reset so same file can be re-uploaded
    e.target.value = '';
  }, [onFile]);

  return (
    <div
      style={{
        border: `2px dashed ${dragging ? '#1a73e8' : '#dadce0'}`,
        borderRadius: 12,
        padding: '32px 24px',
        textAlign: 'center',
        cursor: disabled ? 'not-allowed' : 'pointer',
        background: dragging ? '#e8f0fe' : '#fafafa',
        transition: 'all 0.2s ease',
        opacity: disabled ? 0.5 : 1,
      }}
      onClick={() => !disabled && inputRef.current?.click()}
      onDragOver={e => { e.preventDefault(); if (!disabled) setDragging(true); }}
      onDragLeave={() => setDragging(false)}
      onDrop={handleDrop}
    >
      <div style={{ fontSize: 40, marginBottom: 8 }}>📂</div>
      <div style={{ fontWeight: 600, fontSize: 15, color: '#202124', marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 13, color: '#5f6368', marginBottom: 8 }}>{hint}</div>
      <div style={{ fontSize: 12, color: '#9aa0a6' }}>拖曳檔案至此，或點擊選擇檔案</div>
      <div style={{ fontSize: 11, color: '#9aa0a6', marginTop: 4 }}>
        支援格式：{accept}
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        style={{ display: 'none' }}
        onChange={handleChange}
        disabled={disabled}
      />
    </div>
  );
}

// ─────────── Upload Result Banner ───────────

function UploadResult({ state, onReset }: { state: UploadState; onReset: () => void }) {
  if (state.status === 'idle') return null;
  if (state.status === 'uploading') {
    return (
      <div className="alert alert-info" style={{ alignItems: 'center' }}>
        <span className="spinner spinner-sm" />
        <span>上傳中，請稍候…</span>
      </div>
    );
  }
  if (state.status === 'success') {
    return (
      <div className="alert alert-success">
        <span>✓</span>
        <span>
          上傳成功！共匯入 <strong>{state.count ?? 0}</strong> 筆資料。
          {state.message && <> {state.message}</>}
        </span>
        <button className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }} onClick={onReset}>×</button>
      </div>
    );
  }
  return (
    <div className="alert alert-danger">
      <span>⚠</span>
      <span>{state.message ?? '上傳失敗，請檢查檔案格式後重試。'}</span>
      <button className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }} onClick={onReset}>×</button>
    </div>
  );
}

// ─────────── Main Page ───────────

export default function ImportPage() {
  // Session selector
  const sessionsQuery = useQuery({
    queryKey: ['sessions'],
    queryFn:  () => scheduleApi.getAllSessions().then(r => r.data.data),
  });
  const sessions = sessionsQuery.data?.content ?? [];

  const [selectedSessionId, setSelectedSessionId] = useState<number>(0);

  // Upload states per panel
  const [requestState,   setRequestState]   = useState<UploadState>({ status: 'idle' });
  const [maintState,     setMaintState]     = useState<UploadState>({ status: 'idle' });

  // Export states
  const [exportLoading, setExportLoading] = useState<'xml' | 'csv' | null>(null);
  const [exportError,   setExportError]   = useState<string | null>(null);

  // ── Upload handlers ──
  const handleRequestUpload = useCallback(async (file: File) => {
    if (!selectedSessionId) {
      setRequestState({ status: 'error', message: '請先選擇目標排程 Session。' });
      return;
    }
    setRequestState({ status: 'uploading' });
    try {
      const res = await importApi.importSatelliteRequests(file, selectedSessionId);
      const data = res.data.data as unknown[];
      setRequestState({ status: 'success', count: data.length });
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })
        ?.response?.data?.message ?? (e as Error).message ?? '未知錯誤';
      setRequestState({ status: 'error', message: msg });
    }
  }, [selectedSessionId]);

  const handleMaintUpload = useCallback(async (file: File) => {
    setMaintState({ status: 'uploading' });
    try {
      const res = await importApi.importStationUnavailabilities(file);
      const data = res.data.data as unknown[];
      setMaintState({ status: 'success', count: data.length });
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })
        ?.response?.data?.message ?? (e as Error).message ?? '未知錯誤';
      setMaintState({ status: 'error', message: msg });
    }
  }, []);

  // ── Export handlers ──
  const handleExport = useCallback(async (format: 'xml' | 'csv') => {
    if (!selectedSessionId) {
      setExportError('請先選擇目標排程 Session。');
      return;
    }
    setExportLoading(format);
    setExportError(null);
    try {
      const res = format === 'xml'
        ? await exportApi.downloadXml(selectedSessionId)
        : await exportApi.downloadCsv(selectedSessionId);
      const session = sessions.find(s => s.id === selectedSessionId);
      const name = session?.name.replace(/\s+/g, '_') ?? `session_${selectedSessionId}`;
      triggerDownload(res.data as Blob, `${name}_schedule.${format}`);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })
        ?.response?.data?.message ?? (e as Error).message ?? '匯出失敗';
      setExportError(msg);
    } finally {
      setExportLoading(null);
    }
  }, [selectedSessionId, sessions]);

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">資料匯入 / 匯出</h1>
          <p className="page-subtitle">上傳衛星需求與地面站維護時段，或下載排程結果</p>
        </div>
      </div>

      {/* Session selector */}
      <div className="card" style={{ marginBottom: 24 }}>
        <div className="card-header">
          <span className="card-title">目標排程 Session</span>
        </div>
        <div className="card-body">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
            <div style={{ flex: '1 1 300px' }}>
              <select
                className="form-control"
                value={selectedSessionId}
                onChange={e => setSelectedSessionId(Number(e.target.value))}
              >
                <option value={0}>— 請選擇 Session（匯入/匯出衛星需求與結果時必選）—</option>
                {sessions.map(s => (
                  <option key={s.id} value={s.id}>
                    [{s.status}] {s.name}
                  </option>
                ))}
              </select>
              <div className="form-hint" style={{ marginTop: 4 }}>
                匯入衛星需求或匯出排程結果時需選擇 Session；匯入維護時段時可不選。
              </div>
            </div>
            {sessionsQuery.isLoading && <span className="spinner spinner-sm" />}
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginBottom: 24 }}>
        {/* ── Import: Satellite Requests ── */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">📥 衛星需求匯入</span>
            <span className="badge badge-info">需選擇 Session</span>
          </div>
          <div className="card-body">
            <p style={{ fontSize: 13, color: '#5f6368', marginBottom: 16 }}>
              上傳 <strong>XML</strong> 或 <strong>CSV</strong> 格式的衛星過境需求檔案。
              系統將解析並匯入至選定的排程 Session。
            </p>

            <UploadResult
              state={requestState}
              onReset={() => setRequestState({ status: 'idle' })}
            />

            <DropZone
              accept=".xml,.csv"
              label="衛星需求檔案"
              hint="支援 XML (schema: satellite-request.xsd) 與 CSV 格式"
              onFile={handleRequestUpload}
              disabled={requestState.status === 'uploading' || !selectedSessionId}
            />

            {!selectedSessionId && (
              <div style={{ marginTop: 8, fontSize: 12, color: '#fbbc04' }}>
                ⚠ 請先選擇目標 Session 才能上傳
              </div>
            )}

            {/* Format reference */}
            <details style={{ marginTop: 16, fontSize: 12 }}>
              <summary style={{ cursor: 'pointer', color: '#1a73e8', fontWeight: 500 }}>
                查看 CSV 格式範例
              </summary>
              <pre style={{
                marginTop: 8, padding: 12,
                background: '#f8f9fa', borderRadius: 6,
                overflowX: 'auto', fontSize: 11, color: '#202124',
                fontFamily: 'Consolas, monospace',
              }}>
{`satelliteCode,groundStationCode,frequencyBand,aos,los,priority
SAT001,GS-TW,X,2026-03-10T08:00:00Z,2026-03-10T08:12:00Z,HIGH
SAT002,GS-JP,S,2026-03-10T09:30:00Z,2026-03-10T09:45:00Z,MEDIUM`}
              </pre>
            </details>
          </div>
        </div>

        {/* ── Import: Station Unavailabilities ── */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">🔧 地面站維護時段匯入</span>
            <span className="badge badge-secondary">Session 可選</span>
          </div>
          <div className="card-body">
            <p style={{ fontSize: 13, color: '#5f6368', marginBottom: 16 }}>
              上傳 <strong>TXT</strong>、<strong>XML</strong> 或 <strong>CSV</strong>{' '}
              格式的地面站維護停機時段，系統將在排程時自動迴避這些時段。
            </p>

            <UploadResult
              state={maintState}
              onReset={() => setMaintState({ status: 'idle' })}
            />

            <DropZone
              accept=".txt,.xml,.csv"
              label="維護時段檔案"
              hint="支援 TXT、XML 與 CSV 格式"
              onFile={handleMaintUpload}
              disabled={maintState.status === 'uploading'}
            />

            <details style={{ marginTop: 16, fontSize: 12 }}>
              <summary style={{ cursor: 'pointer', color: '#1a73e8', fontWeight: 500 }}>
                查看 TXT 格式範例
              </summary>
              <pre style={{
                marginTop: 8, padding: 12,
                background: '#f8f9fa', borderRadius: 6,
                overflowX: 'auto', fontSize: 11, color: '#202124',
                fontFamily: 'Consolas, monospace',
              }}>
{`# Ground Station Unavailability
# Format: STATION_CODE | START_TIME | END_TIME | REASON
GS-TW | 2026-03-12T00:00:00Z | 2026-03-12T06:00:00Z | 天線維修
GS-JP | 2026-03-14T12:00:00Z | 2026-03-14T18:00:00Z | 系統升級`}
              </pre>
            </details>
          </div>
        </div>
      </div>

      {/* ── Export ── */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">📤 排程結果匯出</span>
          <span className="badge badge-info">需選擇 Session</span>
        </div>
        <div className="card-body">
          <p style={{ fontSize: 13, color: '#5f6368', marginBottom: 16 }}>
            下載指定 Session 的排程結果。請先在上方選擇 Session，再點擊下方格式按鈕。
          </p>

          {exportError && (
            <div className="alert alert-danger" style={{ marginBottom: 16 }}>
              <span>⚠</span>
              <span>{exportError}</span>
              <button className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }} onClick={() => setExportError(null)}>×</button>
            </div>
          )}

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            {/* XML */}
            <div
              className="card"
              style={{
                flex: '1 1 200px',
                border: '1px solid #dadce0',
                borderRadius: 8,
                padding: 20,
                textAlign: 'center',
                cursor: selectedSessionId ? 'pointer' : 'default',
                opacity: selectedSessionId ? 1 : 0.5,
                transition: 'box-shadow 0.2s',
              }}
              onClick={() => selectedSessionId && handleExport('xml')}
            >
              <div style={{ fontSize: 32, marginBottom: 8 }}>📄</div>
              <div style={{ fontWeight: 600, marginBottom: 4 }}>XML 格式</div>
              <div style={{ fontSize: 12, color: '#5f6368', marginBottom: 12 }}>
                符合 XSD Schema 的標準 XML 格式，適合系統間資料交換
              </div>
              <button
                className="btn btn-primary btn-sm"
                disabled={!selectedSessionId || exportLoading === 'xml'}
                onClick={e => { e.stopPropagation(); handleExport('xml'); }}
              >
                {exportLoading === 'xml'
                  ? <><span className="spinner spinner-sm" /> 下載中…</>
                  : '⬇ 下載 XML'}
              </button>
            </div>

            {/* CSV */}
            <div
              className="card"
              style={{
                flex: '1 1 200px',
                border: '1px solid #dadce0',
                borderRadius: 8,
                padding: 20,
                textAlign: 'center',
                cursor: selectedSessionId ? 'pointer' : 'default',
                opacity: selectedSessionId ? 1 : 0.5,
                transition: 'box-shadow 0.2s',
              }}
              onClick={() => selectedSessionId && handleExport('csv')}
            >
              <div style={{ fontSize: 32, marginBottom: 8 }}>📊</div>
              <div style={{ fontWeight: 600, marginBottom: 4 }}>CSV 格式</div>
              <div style={{ fontSize: 12, color: '#5f6368', marginBottom: 12 }}>
                逗號分隔格式，可直接以 Excel 或試算表軟體開啟
              </div>
              <button
                className="btn btn-secondary btn-sm"
                disabled={!selectedSessionId || exportLoading === 'csv'}
                onClick={e => { e.stopPropagation(); handleExport('csv'); }}
              >
                {exportLoading === 'csv'
                  ? <><span className="spinner spinner-sm" /> 下載中…</>
                  : '⬇ 下載 CSV'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
