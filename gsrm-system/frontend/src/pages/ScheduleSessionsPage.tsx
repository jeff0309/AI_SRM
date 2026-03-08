import React, { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { scheduleApi } from '../api/scheduleApi';
import { satelliteApi } from '../api/satelliteApi';
import { groundStationApi } from '../api/groundStationApi';
import GanttChart from '../components/gantt/GanttChart';
import type {
  ScheduleSession,
  ScheduleSessionRequest,
  ScheduleResultResponse,
  ScheduleStatus,
} from '../types';

// ─────────── helpers ───────────

const STATUS_LABEL: Record<ScheduleStatus, string> = {
  DRAFT:      '草稿',
  PROCESSING: '執行中',
  COMPLETED:  '已完成',
  FAILED:     '失敗',
};

const STATUS_BADGE: Record<ScheduleStatus, string> = {
  DRAFT:      'badge-secondary',
  PROCESSING: 'badge-info',
  COMPLETED:  'badge-success',
  FAILED:     'badge-danger',
};

function fmt(iso: string) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
}

function fmtDatetimeLocal(iso: string) {
  // Convert ISO to datetime-local input format (YYYY-MM-DDTHH:mm)
  if (!iso) return '';
  return iso.slice(0, 16);
}

// ─────────── Session Form ───────────

interface SessionFormProps {
  initial?: Partial<ScheduleSessionRequest>;
  onSubmit: (data: ScheduleSessionRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}

function SessionForm({ initial, onSubmit, onCancel, loading }: SessionFormProps) {
  const now = new Date();
  const weekLater = new Date(now.getTime() + 7 * 86_400_000);
  const defaultStart = now.toISOString().slice(0, 16);
  const defaultEnd   = weekLater.toISOString().slice(0, 16);

  const [form, setForm] = useState<ScheduleSessionRequest>({
    name:               initial?.name               ?? '',
    description:        initial?.description         ?? '',
    scheduleStartTime:  initial?.scheduleStartTime   ?? defaultStart,
    scheduleEndTime:    initial?.scheduleEndTime     ?? defaultEnd,
    shorteningStrategy: initial?.shorteningStrategy  ?? 'FAIR_SHARE',
    satelliteIds:       initial?.satelliteIds        ?? [],
    groundStationIds:   initial?.groundStationIds    ?? [],
  });
  const [errors, setErrors] = useState<Partial<Record<keyof ScheduleSessionRequest, string>>>({});

  // Load enabled satellites and ground stations
  const { data: satList = [] } = useQuery({
    queryKey: ['satellites-enabled'],
    queryFn: () => satelliteApi.getEnabled().then(r => r.data.data),
  });
  const { data: gsList = [] } = useQuery({
    queryKey: ['ground-stations-enabled'],
    queryFn: () => groundStationApi.getEnabled().then(r => r.data.data),
  });

  const toggleId = (field: 'satelliteIds' | 'groundStationIds', id: number) => {
    setForm(prev => {
      const cur = prev[field] ?? [];
      const next = cur.includes(id) ? cur.filter(x => x !== id) : [...cur, id];
      return { ...prev, [field]: next };
    });
  };

  const set = (field: keyof ScheduleSessionRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
      setForm(prev => ({ ...prev, [field]: e.target.value }));

  const validate = (): boolean => {
    const errs: typeof errors = {};
    if (!form.name.trim())            errs.name = '名稱為必填';
    if (!form.scheduleStartTime)      errs.scheduleStartTime = '開始時間為必填';
    if (!form.scheduleEndTime)        errs.scheduleEndTime = '結束時間為必填';
    if (form.scheduleStartTime && form.scheduleEndTime &&
        form.scheduleStartTime >= form.scheduleEndTime) {
      errs.scheduleEndTime = '結束時間必須晚於開始時間';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      ...form,
      scheduleStartTime: new Date(form.scheduleStartTime).toISOString(),
      scheduleEndTime:   new Date(form.scheduleEndTime).toISOString(),
    });
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="form-group">
        <label className="form-label required">Session 名稱</label>
        <input
          className={`form-control ${errors.name ? 'is-invalid' : ''}`}
          value={form.name}
          onChange={set('name')}
          placeholder="例：2026-W11 週排程"
          maxLength={100}
        />
        {errors.name && <span className="form-error">{errors.name}</span>}
      </div>

      <div className="form-group">
        <label className="form-label">說明</label>
        <textarea
          className="form-control"
          value={form.description}
          onChange={set('description')}
          placeholder="選填說明文字"
          rows={2}
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div className="form-group">
          <label className="form-label required">排程開始</label>
          <input
            type="datetime-local"
            className={`form-control ${errors.scheduleStartTime ? 'is-invalid' : ''}`}
            value={fmtDatetimeLocal(form.scheduleStartTime)}
            onChange={set('scheduleStartTime')}
          />
          {errors.scheduleStartTime && (
            <span className="form-error">{errors.scheduleStartTime}</span>
          )}
        </div>
        <div className="form-group">
          <label className="form-label required">排程結束</label>
          <input
            type="datetime-local"
            className={`form-control ${errors.scheduleEndTime ? 'is-invalid' : ''}`}
            value={fmtDatetimeLocal(form.scheduleEndTime)}
            onChange={set('scheduleEndTime')}
          />
          {errors.scheduleEndTime && (
            <span className="form-error">{errors.scheduleEndTime}</span>
          )}
        </div>
      </div>

      {/* ── Satellite selection ── */}
      <div className="form-group">
        <label className="form-label">
          參與衛星
          <span style={{ marginLeft: 8, fontSize: 11, fontWeight: 400, color: '#94a3b8' }}>
            已選 {(form.satelliteIds ?? []).length} / {satList.length}
          </span>
        </label>
        {satList.length === 0 ? (
          <div style={{ fontSize: 12, color: '#94a3b8', padding: '8px 0' }}>
            尚無啟用中的衛星，請先至「衛星管理」新增。
          </div>
        ) : (
          <div style={{
            border: '1px solid #e2e8f0', borderRadius: 6, maxHeight: 160,
            overflowY: 'auto', padding: '6px 0',
          }}>
            {satList.map(s => {
              const checked = (form.satelliteIds ?? []).includes(s.id);
              return (
                <label key={s.id} style={{
                  display: 'flex', alignItems: 'center', gap: 8,
                  padding: '5px 12px', cursor: 'pointer',
                  background: checked ? '#eff6ff' : 'transparent',
                  transition: 'background 0.1s',
                }}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => toggleId('satelliteIds', s.id)}
                    style={{ accentColor: '#3b82f6' }}
                  />
                  <span style={{ fontSize: 13, color: '#0f172a', flex: 1 }}>{s.name}</span>
                  {s.isEmergency && (
                    <span style={{ fontSize: 10, color: '#dc2626', fontWeight: 700 }}>⚡緊急</span>
                  )}
                  <span style={{
                    fontSize: 10, fontWeight: 700, padding: '1px 6px', borderRadius: 999,
                    background: s.frequencyBand === 'X' ? '#dbeafe' : s.frequencyBand === 'S' ? '#dcfce7' : '#f3e8ff',
                    color: s.frequencyBand === 'X' ? '#1e40af' : s.frequencyBand === 'S' ? '#166534' : '#6b21a8',
                  }}>{s.frequencyBand}</span>
                </label>
              );
            })}
          </div>
        )}
        {satList.length > 0 && (
          <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost btn-sm"
              onClick={() => setForm(p => ({ ...p, satelliteIds: satList.map(s => s.id) }))}>
              全選
            </button>
            <button type="button" className="btn btn-ghost btn-sm"
              onClick={() => setForm(p => ({ ...p, satelliteIds: [] }))}>
              清除
            </button>
          </div>
        )}
      </div>

      {/* ── Ground station selection ── */}
      <div className="form-group">
        <label className="form-label">
          參與地面站
          <span style={{ marginLeft: 8, fontSize: 11, fontWeight: 400, color: '#94a3b8' }}>
            已選 {(form.groundStationIds ?? []).length} / {gsList.length}
          </span>
        </label>
        {gsList.length === 0 ? (
          <div style={{ fontSize: 12, color: '#94a3b8', padding: '8px 0' }}>
            尚無啟用中的地面站，請先至「地面站管理」新增。
          </div>
        ) : (
          <div style={{
            border: '1px solid #e2e8f0', borderRadius: 6, maxHeight: 160,
            overflowY: 'auto', padding: '6px 0',
          }}>
            {gsList.map(gs => {
              const checked = (form.groundStationIds ?? []).includes(gs.id);
              return (
                <label key={gs.id} style={{
                  display: 'flex', alignItems: 'center', gap: 8,
                  padding: '5px 12px', cursor: 'pointer',
                  background: checked ? '#eff6ff' : 'transparent',
                  transition: 'background 0.1s',
                }}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => toggleId('groundStationIds', gs.id)}
                    style={{ accentColor: '#3b82f6' }}
                  />
                  <span style={{ fontSize: 13, color: '#0f172a', flex: 1 }}>{gs.name}</span>
                  {gs.code && (
                    <span style={{ fontSize: 11, color: '#64748b', fontFamily: 'monospace' }}>{gs.code}</span>
                  )}
                  <span style={{
                    fontSize: 10, fontWeight: 700, padding: '1px 6px', borderRadius: 999,
                    background: gs.frequencyBand === 'X' ? '#dbeafe' : gs.frequencyBand === 'S' ? '#dcfce7' : '#f3e8ff',
                    color: gs.frequencyBand === 'X' ? '#1e40af' : gs.frequencyBand === 'S' ? '#166534' : '#6b21a8',
                  }}>{gs.frequencyBand}</span>
                </label>
              );
            })}
          </div>
        )}
        {gsList.length > 0 && (
          <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost btn-sm"
              onClick={() => setForm(p => ({ ...p, groundStationIds: gsList.map(gs => gs.id) }))}>
              全選
            </button>
            <button type="button" className="btn btn-ghost btn-sm"
              onClick={() => setForm(p => ({ ...p, groundStationIds: [] }))}>
              清除
            </button>
          </div>
        )}
      </div>

      <div className="form-group">
        <label className="form-label">衝突縮短策略</label>
        <select className="form-control" value={form.shorteningStrategy} onChange={set('shorteningStrategy')}>
          <option value="FAIR_SHARE">Fair Share（均分縮短）</option>
          <option value="PRIORITY_BASED">Priority Based（依優先權縮短）</option>
          <option value="EMERGENCY_FIRST">Emergency First（緊急任務優先）</option>
        </select>
        <span className="form-hint">排程引擎遇到衝突時採用的縮短演算法</span>
      </div>

      <div className="modal-footer" style={{ padding: 0, paddingTop: 16 }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={loading}>
          取消
        </button>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? <><span className="spinner spinner-sm" /> 儲存中…</> : '儲存'}
        </button>
      </div>
    </form>
  );
}

// ─────────── Result Summary Modal ───────────

interface ResultModalProps {
  result: ScheduleResultResponse;
  onClose: () => void;
}

function ResultModal({ result, onClose }: ResultModalProps) {
  const successPct = result.successRate != null
    ? `${(result.successRate * 100).toFixed(1)}%`
    : '—';

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">排程執行結果</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <div className="stats-grid" style={{ marginBottom: 16 }}>
            <div className="stat-card">
              <div className="stat-card-label">總需求數</div>
              <div className="stat-card-value">{result.totalRequests}</div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">已排程</div>
              <div className="stat-card-value" style={{ color: '#34a853' }}>{result.scheduledCount}</div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">已縮短</div>
              <div className="stat-card-value" style={{ color: '#fbbc04' }}>{result.shortenedCount}</div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">已拒絕</div>
              <div className="stat-card-value" style={{ color: '#ea4335' }}>{result.rejectedCount}</div>
            </div>
          </div>

          <table>
            <tbody>
              <tr><td style={{ color: '#5f6368', paddingBottom: 8, width: 160 }}>成功率</td><td>{successPct}</td></tr>
              <tr><td style={{ color: '#5f6368', paddingBottom: 8 }}>衝突解決數</td><td>{result.conflictsResolved}</td></tr>
              <tr><td style={{ color: '#5f6368', paddingBottom: 8 }}>緊急強制數</td><td>{result.forcedCount}</td></tr>
              <tr><td style={{ color: '#5f6368', paddingBottom: 8 }}>使用策略</td><td>{result.strategyUsed}</td></tr>
              <tr><td style={{ color: '#5f6368', paddingBottom: 8 }}>執行耗時</td><td>{result.executionTimeMs} ms</td></tr>
              <tr><td style={{ color: '#5f6368' }}>執行時間</td><td>{fmt(result.executedAt ?? '')}</td></tr>
            </tbody>
          </table>

          <div style={{ marginTop: 16 }}>
            <div style={{ marginBottom: 6, fontSize: 13, color: '#5f6368' }}>
              成功率 {successPct}
            </div>
            <div className="progress">
              <div
                className={`progress-bar ${
                  result.successRate >= 0.9 ? 'progress-bar-success' :
                  result.successRate >= 0.7 ? 'progress-bar-warning' :
                  'progress-bar-danger'
                }`}
                style={{ width: `${(result.successRate ?? 0) * 100}%` }}
              />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-primary" onClick={onClose}>關閉</button>
        </div>
      </div>
    </div>
  );
}

// ─────────── Main Page ───────────

export default function ScheduleSessionsPage() {
  const qc = useQueryClient();

  // ── State ──
  const [showCreate, setShowCreate]           = useState(false);
  const [editSession, setEditSession]         = useState<ScheduleSession | null>(null);
  const [ganttSessionId, setGanttSessionId]   = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget]       = useState<ScheduleSession | null>(null);
  const [executeResult, setExecuteResult]     = useState<ScheduleResultResponse | null>(null);
  const [executeTargetId, setExecuteTargetId] = useState<number | null>(null);
  const [error, setError]                     = useState<string | null>(null);

  // ── Queries ──
  const sessionsQuery = useQuery({
    queryKey: ['sessions'],
    queryFn:  () => scheduleApi.getAllSessions().then(r => r.data.data),
  });

  // ── Mutations ──
  const createMut = useMutation({
    mutationFn: (data: ScheduleSessionRequest) => scheduleApi.createSession(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['sessions'] }); setShowCreate(false); },
    onError:   (e: Error) => setError(e.message),
  });

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ScheduleSessionRequest }) =>
      scheduleApi.updateSession(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['sessions'] }); setEditSession(null); },
    onError:   (e: Error) => setError(e.message),
  });

  const deleteMut = useMutation({
    mutationFn: (id: number) => scheduleApi.deleteSession(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['sessions'] }); setDeleteTarget(null); },
    onError:   (e: Error) => setError(e.message),
  });

  const executeMut = useMutation({
    mutationFn: (id: number) => scheduleApi.executeSchedule(id),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ['sessions'] });
      setExecuteResult(res.data.data);
      setExecuteTargetId(null);
    },
    onError: (e: Error) => { setError(e.message); setExecuteTargetId(null); },
  });

  const resetMut = useMutation({
    mutationFn: (id: number) => scheduleApi.resetSchedule(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['sessions'] }),
    onError:   (e: Error) => setError(e.message),
  });

  // ── Handlers ──
  const handleExecute = useCallback((session: ScheduleSession) => {
    setExecuteTargetId(session.id);
    executeMut.mutate(session.id);
  }, [executeMut]);

  const handleReset = useCallback((session: ScheduleSession) => {
    if (!window.confirm(`確定要重置 Session「${session.name}」嗎？此操作將清除所有已排程的 Pass。`)) return;
    resetMut.mutate(session.id);
  }, [resetMut]);

  const sessions = sessionsQuery.data?.content ?? [];

  // ─── Gantt view ───
  if (ganttSessionId != null) {
    const session = sessions.find(s => s.id === ganttSessionId);
    return (
      <div className="page-container">
        <div className="page-header">
          <div>
            <button className="btn btn-secondary btn-sm" onClick={() => setGanttSessionId(null)}>
              ← 返回列表
            </button>
            <h1 className="page-title" style={{ marginTop: 8 }}>
              甘特圖：{session?.name ?? `Session #${ganttSessionId}`}
            </h1>
          </div>
        </div>
        <GanttChart sessionId={ganttSessionId} />
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">排程 Session</h1>
          <p className="page-subtitle">管理週排程循環，執行排程引擎，檢視甘特圖</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          + 建立 Session
        </button>
      </div>

      {/* Error banner */}
      {error && (
        <div className="alert alert-danger" style={{ marginBottom: 16 }}>
          <span>⚠</span>
          <span>{error}</span>
          <button
            className="btn btn-ghost btn-sm"
            style={{ marginLeft: 'auto' }}
            onClick={() => setError(null)}
          >
            ×
          </button>
        </div>
      )}

      {/* Stats */}
      {sessions.length > 0 && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-card-label">總 Session 數</div>
            <div className="stat-card-value">{sessionsQuery.data?.totalElements ?? 0}</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">已完成</div>
            <div className="stat-card-value">{sessions.filter(s => s.status === 'COMPLETED').length}</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">草稿</div>
            <div className="stat-card-value">{sessions.filter(s => s.status === 'DRAFT').length}</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-label">失敗</div>
            <div className="stat-card-value" style={{ color: '#ea4335' }}>
              {sessions.filter(s => s.status === 'FAILED').length}
            </div>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="card">
        {sessionsQuery.isLoading ? (
          <div className="loading-overlay">
            <div className="spinner spinner-lg" />
            <span>載入中…</span>
          </div>
        ) : sessions.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📅</div>
            <div className="empty-state-title">尚無排程 Session</div>
            <div className="empty-state-description">
              點擊「建立 Session」開始新增週排程
            </div>
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              + 建立 Session
            </button>
          </div>
        ) : (
          <div className="table-wrapper" style={{ borderRadius: 0, border: 'none' }}>
            <table>
              <thead>
                <tr>
                  <th>Session 名稱</th>
                  <th>排程區間</th>
                  <th>狀態</th>
                  <th style={{ textAlign: 'right' }}>需求 / 排程 / 拒絕</th>
                  <th>建立時間</th>
                  <th style={{ textAlign: 'center' }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map(session => (
                  <tr key={session.id}>
                    <td>
                      <div style={{ fontWeight: 500 }}>{session.name}</div>
                      {session.description && (
                        <div style={{ fontSize: 12, color: '#5f6368' }}>{session.description}</div>
                      )}
                    </td>
                    <td>
                      <div style={{ fontSize: 12 }}>{fmt(session.scheduleStartTime)}</div>
                      <div style={{ fontSize: 12, color: '#5f6368' }}>→ {fmt(session.scheduleEndTime)}</div>
                    </td>
                    <td>
                      <span className={`badge ${STATUS_BADGE[session.status]}`}>
                        {STATUS_LABEL[session.status]}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>
                      {session.totalRequests > 0 ? (
                        <span>
                          {session.totalRequests} / <span style={{ color: '#34a853' }}>{session.scheduledCount}</span>
                          {' '}/ <span style={{ color: '#ea4335' }}>{session.rejectedCount}</span>
                        </span>
                      ) : (
                        <span style={{ color: '#9aa0a6' }}>—</span>
                      )}
                    </td>
                    <td style={{ fontSize: 12 }}>{fmt(session.createdAt)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4, justifyContent: 'center', flexWrap: 'wrap' }}>
                        {/* Gantt */}
                        <button
                          className="btn btn-ghost btn-sm"
                          title="查看甘特圖"
                          onClick={() => setGanttSessionId(session.id)}
                        >
                          📊 甘特圖
                        </button>

                        {/* Execute */}
                        {(session.status === 'DRAFT' || session.status === 'FAILED') && (
                          <button
                            className="btn btn-success btn-sm"
                            disabled={executeTargetId === session.id && executeMut.isPending}
                            onClick={() => handleExecute(session)}
                            title="執行排程"
                          >
                            {executeTargetId === session.id && executeMut.isPending
                              ? <><span className="spinner spinner-sm" /> 執行中</>
                              : '▶ 執行'}
                          </button>
                        )}

                        {/* Reset */}
                        {session.status === 'COMPLETED' && (
                          <button
                            className="btn btn-warning btn-sm"
                            onClick={() => handleReset(session)}
                            disabled={resetMut.isPending}
                            title="重置排程"
                          >
                            ↺ 重置
                          </button>
                        )}

                        {/* Edit */}
                        {session.status === 'DRAFT' && (
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => setEditSession(session)}
                            title="編輯"
                          >
                            ✏ 編輯
                          </button>
                        )}

                        {/* Delete */}
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => setDeleteTarget(session)}
                          title="刪除"
                        >
                          🗑
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Create Modal ── */}
      {showCreate && (
        <div className="modal-backdrop" onClick={() => setShowCreate(false)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">建立排程 Session</h3>
              <button className="modal-close" onClick={() => setShowCreate(false)}>×</button>
            </div>
            <div className="modal-body">
              <SessionForm
                onSubmit={data => createMut.mutate(data)}
                onCancel={() => setShowCreate(false)}
                loading={createMut.isPending}
              />
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ── */}
      {editSession && (
        <div className="modal-backdrop" onClick={() => setEditSession(null)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">編輯 Session</h3>
              <button className="modal-close" onClick={() => setEditSession(null)}>×</button>
            </div>
            <div className="modal-body">
              <SessionForm
                initial={{
                  name:               editSession.name,
                  description:        editSession.description,
                  scheduleStartTime:  editSession.scheduleStartTime,
                  scheduleEndTime:    editSession.scheduleEndTime,
                  shorteningStrategy: editSession.shorteningStrategy,
                  satelliteIds:       editSession.satelliteIds,
                  groundStationIds:   editSession.groundStationIds,
                }}
                onSubmit={data => updateMut.mutate({ id: editSession.id, data })}
                onCancel={() => setEditSession(null)}
                loading={updateMut.isPending}
              />
            </div>
          </div>
        </div>
      )}

      {/* ── Delete Confirm Modal ── */}
      {deleteTarget && (
        <div className="modal-backdrop" onClick={() => setDeleteTarget(null)}>
          <div className="modal" style={{ maxWidth: 420 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">確認刪除</h3>
              <button className="modal-close" onClick={() => setDeleteTarget(null)}>×</button>
            </div>
            <div className="modal-body">
              <p>
                確定要刪除 Session「<strong>{deleteTarget.name}</strong>」嗎？
                <br />此操作無法還原，所有相關 Pass 也將一併刪除。
              </p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setDeleteTarget(null)}>取消</button>
              <button
                className="btn btn-danger"
                disabled={deleteMut.isPending}
                onClick={() => deleteMut.mutate(deleteTarget.id)}
              >
                {deleteMut.isPending ? <><span className="spinner spinner-sm" /> 刪除中…</> : '確認刪除'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Execute Result Modal ── */}
      {executeResult && (
        <ResultModal result={executeResult} onClose={() => setExecuteResult(null)} />
      )}
    </div>
  );
}
