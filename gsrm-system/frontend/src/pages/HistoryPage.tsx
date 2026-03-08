import React, { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { scheduleApi } from '../api/scheduleApi';
import { exportApi, triggerDownload } from '../api/importExportApi';
import type { ScheduleSession, ScheduleStatus, PassStatus } from '../types';

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

const PASS_STATUS_LABEL: Record<PassStatus, string> = {
  PENDING:   '待定',
  SCHEDULED: '已排程',
  SHORTENED: '已縮短',
  REJECTED:  '已拒絕',
  FORCED:    '強制',
};

const PASS_STATUS_BADGE: Record<PassStatus, string> = {
  PENDING:   'badge-secondary',
  SCHEDULED: 'badge-success',
  SHORTENED: 'badge-warning',
  REJECTED:  'badge-danger',
  FORCED:    'badge-info',
};

function fmt(iso: string) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
}

function duration(start: string, end: string): string {
  const diffMs = new Date(end).getTime() - new Date(start).getTime();
  const days   = Math.floor(diffMs / 86_400_000);
  const hours  = Math.floor((diffMs % 86_400_000) / 3_600_000);
  const mins   = Math.floor((diffMs % 3_600_000) / 60_000);
  if (days > 0) return `${days}天 ${hours}h`;
  if (hours > 0) return `${hours}h ${mins}m`;
  return `${mins}m`;
}

// ─────────── Session Detail Panel ───────────

interface SessionDetailProps {
  session: ScheduleSession;
  onClose: () => void;
}

function SessionDetail({ session, onClose }: SessionDetailProps) {
  const passesQuery = useQuery({
    queryKey: ['session-passes', session.id],
    queryFn:  () => scheduleApi.getPasses(session.id).then(r => r.data.data as Record<string, unknown>[]),
    enabled:  session.status === 'COMPLETED',
  });

  const [passFilter, setPassFilter] = useState<PassStatus | 'ALL'>('ALL');
  const [exportLoading, setExportLoading] = useState<'xml' | 'csv' | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);

  const passes = passesQuery.data ?? [];
  const filtered = useMemo(() =>
    passFilter === 'ALL'
      ? passes
      : passes.filter(p => (p as { status?: string }).status === passFilter),
    [passes, passFilter]
  );

  const handleExport = async (format: 'xml' | 'csv') => {
    setExportLoading(format);
    setExportError(null);
    try {
      const res = format === 'xml'
        ? await exportApi.downloadXml(session.id)
        : await exportApi.downloadCsv(session.id);
      const name = session.name.replace(/\s+/g, '_');
      triggerDownload(res.data as Blob, `${name}.${format}`);
    } catch (e: unknown) {
      setExportError((e as Error).message ?? '匯出失敗');
    } finally {
      setExportLoading(null);
    }
  };

  const successRate = session.totalRequests > 0
    ? ((session.scheduledCount / session.totalRequests) * 100).toFixed(1)
    : '—';

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal modal-xl" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3 className="modal-title">{session.name}</h3>
            <div style={{ fontSize: 12, color: '#5f6368', marginTop: 2 }}>
              {fmt(session.scheduleStartTime)} → {fmt(session.scheduleEndTime)}
              &nbsp;（{duration(session.scheduleStartTime, session.scheduleEndTime)}）
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {session.status === 'COMPLETED' && (
              <>
                <button
                  className="btn btn-secondary btn-sm"
                  disabled={exportLoading === 'xml'}
                  onClick={() => handleExport('xml')}
                >
                  {exportLoading === 'xml' ? <><span className="spinner spinner-sm" /> 匯出…</> : '⬇ XML'}
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  disabled={exportLoading === 'csv'}
                  onClick={() => handleExport('csv')}
                >
                  {exportLoading === 'csv' ? <><span className="spinner spinner-sm" /> 匯出…</> : '⬇ CSV'}
                </button>
              </>
            )}
            <button className="modal-close" onClick={onClose}>×</button>
          </div>
        </div>

        <div className="modal-body">
          {/* Summary stats */}
          <div className="stats-grid" style={{ marginBottom: 16 }}>
            <div className="stat-card">
              <div className="stat-card-label">總需求</div>
              <div className="stat-card-value">{session.totalRequests || '—'}</div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">已排程</div>
              <div className="stat-card-value" style={{ color: '#34a853' }}>
                {session.scheduledCount || 0}
              </div>
              <div className="stat-card-sub">成功率 {successRate}{session.totalRequests > 0 ? '%' : ''}</div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">已拒絕</div>
              <div className="stat-card-value" style={{ color: '#ea4335' }}>
                {session.rejectedCount || 0}
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-card-label">策略</div>
              <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>
                {session.shorteningStrategy || '—'}
              </div>
            </div>
          </div>

          {/* Progress bar */}
          {session.totalRequests > 0 && (
            <div style={{ marginBottom: 16 }}>
              <div style={{ fontSize: 12, color: '#5f6368', marginBottom: 4 }}>
                成功率 {successRate}%
              </div>
              <div className="progress">
                <div
                  className={`progress-bar ${
                    Number(successRate) >= 90 ? 'progress-bar-success' :
                    Number(successRate) >= 70 ? 'progress-bar-warning' :
                    'progress-bar-danger'
                  }`}
                  style={{ width: `${successRate}%` }}
                />
              </div>
            </div>
          )}

          {exportError && (
            <div className="alert alert-danger" style={{ marginBottom: 12 }}>
              {exportError}
            </div>
          )}

          {/* Pass list */}
          {session.status === 'COMPLETED' && (
            <>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                <h4 style={{ fontSize: 14, fontWeight: 600 }}>Pass 列表</h4>
                <select
                  className="form-control"
                  style={{ width: 'auto', padding: '4px 8px', fontSize: 12, height: 30 }}
                  value={passFilter}
                  onChange={e => setPassFilter(e.target.value as PassStatus | 'ALL')}
                >
                  <option value="ALL">全部狀態</option>
                  {(Object.keys(PASS_STATUS_LABEL) as PassStatus[]).map(s => (
                    <option key={s} value={s}>{PASS_STATUS_LABEL[s]}</option>
                  ))}
                </select>
              </div>

              {passesQuery.isLoading ? (
                <div className="loading-overlay" style={{ padding: 32 }}>
                  <div className="spinner" />
                  <span>載入 Pass 資料中…</span>
                </div>
              ) : filtered.length === 0 ? (
                <div className="empty-state" style={{ padding: 32 }}>
                  <div className="empty-state-icon">📭</div>
                  <div className="empty-state-title">無符合條件的 Pass</div>
                </div>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>衛星</th>
                        <th>地面站</th>
                        <th>頻段</th>
                        <th>AOS（排程後）</th>
                        <th>LOS（排程後）</th>
                        <th>時長</th>
                        <th>狀態</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filtered.map((p, idx) => {
                        const pass = p as {
                          passId?: number;
                          satelliteName?: string;
                          groundStationName?: string;
                          frequencyBand?: string;
                          scheduledAos?: string;
                          scheduledLos?: string;
                          durationSeconds?: number;
                          status?: PassStatus;
                        };
                        const dur = pass.durationSeconds
                          ? `${Math.floor(pass.durationSeconds / 60)}m ${pass.durationSeconds % 60}s`
                          : '—';
                        return (
                          <tr key={pass.passId ?? idx}>
                            <td style={{ fontWeight: 500 }}>{pass.satelliteName ?? '—'}</td>
                            <td>{pass.groundStationName ?? '—'}</td>
                            <td>
                              <span className="badge badge-secondary">{pass.frequencyBand ?? '—'}</span>
                            </td>
                            <td style={{ fontSize: 12 }}>{fmt(pass.scheduledAos ?? '')}</td>
                            <td style={{ fontSize: 12 }}>{fmt(pass.scheduledLos ?? '')}</td>
                            <td style={{ fontSize: 12, fontVariantNumeric: 'tabular-nums' }}>{dur}</td>
                            <td>
                              {pass.status && (
                                <span className={`badge ${PASS_STATUS_BADGE[pass.status]}`}>
                                  {PASS_STATUS_LABEL[pass.status]}
                                </span>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}

          {session.status !== 'COMPLETED' && (
            <div className="alert alert-info">
              此 Session 尚未完成排程，無 Pass 詳細資料可顯示。
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ─────────── Main Page ───────────

export default function HistoryPage() {
  const [filterStatus, setFilterStatus] = useState<ScheduleStatus | 'ALL'>('ALL');
  const [searchText,   setSearchText]   = useState('');
  const [selectedSession, setSelectedSession] = useState<ScheduleSession | null>(null);

  const sessionsQuery = useQuery({
    queryKey: ['sessions'],
    queryFn:  () => scheduleApi.getAllSessions(0, 100).then(r => r.data.data),
  });

  const sessions = useMemo(() => {
    const all = sessionsQuery.data?.content ?? [];
    return all.filter(s => {
      const matchStatus = filterStatus === 'ALL' || s.status === filterStatus;
      const matchText   = !searchText ||
        s.name.toLowerCase().includes(searchText.toLowerCase()) ||
        (s.description ?? '').toLowerCase().includes(searchText.toLowerCase());
      return matchStatus && matchText;
    });
  }, [sessionsQuery.data, filterStatus, searchText]);

  // ── Stats summary ──
  const allSessions = sessionsQuery.data?.content ?? [];
  const total     = allSessions.length;
  const completed = allSessions.filter(s => s.status === 'COMPLETED').length;
  const totalPasses = allSessions.reduce((sum, s) => sum + (s.scheduledCount ?? 0), 0);
  const avgSuccess = completed > 0
    ? (allSessions
        .filter(s => s.status === 'COMPLETED' && s.totalRequests > 0)
        .reduce((sum, s) => sum + s.scheduledCount / s.totalRequests, 0) / completed * 100
      ).toFixed(1)
    : '—';

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">歷史查詢</h1>
          <p className="page-subtitle">查閱所有排程 Session 的執行記錄與 Pass 詳情</p>
        </div>
      </div>

      {/* Summary stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-card-label">總 Session 數</div>
          <div className="stat-card-value">{total}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card-label">已完成</div>
          <div className="stat-card-value" style={{ color: '#34a853' }}>{completed}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card-label">累計排程 Pass</div>
          <div className="stat-card-value">{totalPasses.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <div className="stat-card-label">平均成功率</div>
          <div className="stat-card-value">{avgSuccess}{avgSuccess !== '—' ? '%' : ''}</div>
        </div>
      </div>

      {/* Filters */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-body" style={{ paddingTop: 12, paddingBottom: 12 }}>
          <div className="search-bar">
            <div className="search-input-wrapper">
              <span className="search-icon">🔍</span>
              <input
                className="search-input"
                placeholder="搜尋 Session 名稱或說明…"
                value={searchText}
                onChange={e => setSearchText(e.target.value)}
              />
            </div>
            <select
              className="form-control"
              style={{ width: 'auto', padding: '7px 12px' }}
              value={filterStatus}
              onChange={e => setFilterStatus(e.target.value as ScheduleStatus | 'ALL')}
            >
              <option value="ALL">全部狀態</option>
              {(Object.keys(STATUS_LABEL) as ScheduleStatus[]).map(s => (
                <option key={s} value={s}>{STATUS_LABEL[s]}</option>
              ))}
            </select>
            {(filterStatus !== 'ALL' || searchText) && (
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => { setFilterStatus('ALL'); setSearchText(''); }}
              >
                清除篩選
              </button>
            )}
            <span style={{ fontSize: 12, color: '#5f6368', marginLeft: 'auto' }}>
              共 {sessions.length} 筆
            </span>
          </div>
        </div>
      </div>

      {/* Session list */}
      {sessionsQuery.isLoading ? (
        <div className="loading-overlay">
          <div className="spinner spinner-lg" />
          <span>載入中…</span>
        </div>
      ) : sessions.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🔎</div>
          <div className="empty-state-title">無符合條件的 Session</div>
          <div className="empty-state-description">
            調整篩選條件，或前往「排程 Session」頁面建立新排程
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {sessions.map(session => {
            const successPct = session.totalRequests > 0
              ? ((session.scheduledCount / session.totalRequests) * 100).toFixed(0)
              : null;

            return (
              <div
                key={session.id}
                className="card"
                style={{ cursor: 'pointer', transition: 'box-shadow 0.15s' }}
                onClick={() => setSelectedSession(session)}
                onMouseEnter={e => (e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.12)')}
                onMouseLeave={e => (e.currentTarget.style.boxShadow = '')}
              >
                <div style={{ padding: '16px 20px', display: 'flex', gap: 20, alignItems: 'center', flexWrap: 'wrap' }}>
                  {/* Status icon */}
                  <div style={{ fontSize: 28, flexShrink: 0 }}>
                    {session.status === 'COMPLETED' ? '✅'
                     : session.status === 'FAILED'  ? '❌'
                     : session.status === 'PROCESSING' ? '⏳'
                     : '📝'}
                  </div>

                  {/* Main info */}
                  <div style={{ flex: 1, minWidth: 200 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontWeight: 600, fontSize: 15 }}>{session.name}</span>
                      <span className={`badge ${STATUS_BADGE[session.status]}`}>
                        {STATUS_LABEL[session.status]}
                      </span>
                    </div>
                    {session.description && (
                      <div style={{ fontSize: 12, color: '#5f6368', marginBottom: 4 }}>
                        {session.description}
                      </div>
                    )}
                    <div style={{ fontSize: 12, color: '#5f6368' }}>
                      {fmt(session.scheduleStartTime)} → {fmt(session.scheduleEndTime)}
                      &nbsp;（{duration(session.scheduleStartTime, session.scheduleEndTime)}）
                    </div>
                  </div>

                  {/* Pass stats */}
                  {session.totalRequests > 0 && (
                    <div style={{ minWidth: 160 }}>
                      <div style={{ display: 'flex', gap: 12, marginBottom: 6, fontSize: 13 }}>
                        <span title="需求">{session.totalRequests} 需求</span>
                        <span style={{ color: '#34a853' }} title="排程">✓ {session.scheduledCount}</span>
                        <span style={{ color: '#ea4335' }} title="拒絕">✗ {session.rejectedCount}</span>
                      </div>
                      {successPct !== null && (
                        <>
                          <div style={{ fontSize: 11, color: '#5f6368', marginBottom: 3 }}>
                            成功率 {successPct}%
                          </div>
                          <div className="progress">
                            <div
                              className={`progress-bar ${
                                Number(successPct) >= 90 ? 'progress-bar-success' :
                                Number(successPct) >= 70 ? 'progress-bar-warning' :
                                'progress-bar-danger'
                              }`}
                              style={{ width: `${successPct}%` }}
                            />
                          </div>
                        </>
                      )}
                    </div>
                  )}

                  {/* Timestamps */}
                  <div style={{ fontSize: 11, color: '#9aa0a6', textAlign: 'right', minWidth: 120 }}>
                    <div>建立 {fmt(session.createdAt)}</div>
                    {session.executedAt && (
                      <div>執行 {fmt(session.executedAt)}</div>
                    )}
                  </div>

                  {/* Arrow */}
                  <div style={{ fontSize: 18, color: '#dadce0', flexShrink: 0 }}>›</div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Session detail modal */}
      {selectedSession && (
        <SessionDetail
          session={selectedSession}
          onClose={() => setSelectedSession(null)}
        />
      )}
    </div>
  );
}
