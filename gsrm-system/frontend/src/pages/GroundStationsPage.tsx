import React, { useEffect, useMemo, useState } from 'react';
import { groundStationApi } from '../api/groundStationApi';
import type { GroundStation, GroundStationRequest, FrequencyBand } from '../types';

// ─────────── constants ───────────

const BANDS: FrequencyBand[] = ['X', 'S', 'XS'];

type SortKey = 'id' | 'name' | 'code' | 'frequencyBand' | 'latitude' | 'setupTime' | 'enabled';
type SortDir = 'asc' | 'desc';

const BAND_CHIP: Record<string, React.CSSProperties> = {
  X:  { background: '#dbeafe', color: '#1e40af', border: '1px solid #93c5fd' },
  S:  { background: '#dcfce7', color: '#166534', border: '1px solid #86efac' },
  XS: { background: '#f3e8ff', color: '#6b21a8', border: '1px solid #d8b4fe' },
};

// ─────────── sub-components ───────────

function SortTh({ label, sk, cur, dir, onSort, right = false }: {
  label: string; sk: SortKey; cur: SortKey; dir: SortDir;
  onSort: (k: SortKey) => void; right?: boolean;
}) {
  const active = cur === sk;
  return (
    <th
      onClick={() => onSort(sk)}
      style={{
        padding: '11px 14px',
        textAlign: right ? 'right' : 'left',
        fontSize: 12,
        fontWeight: 600,
        letterSpacing: '0.04em',
        textTransform: 'uppercase',
        color: active ? '#1d4ed8' : '#64748b',
        background: active ? '#eff6ff' : '#f8fafc',
        borderBottom: `2px solid ${active ? '#3b82f6' : '#e2e8f0'}`,
        cursor: 'pointer',
        userSelect: 'none',
        whiteSpace: 'nowrap',
        transition: 'background 0.15s, color 0.15s',
      }}
    >
      {label}
      <span style={{ marginLeft: 4, fontSize: 10, color: active ? '#3b82f6' : '#cbd5e1' }}>
        {active ? (dir === 'asc' ? '▲' : '▼') : '⇅'}
      </span>
    </th>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 14 }}>
      <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#475569', marginBottom: 4 }}>
        {label}
      </label>
      {children}
    </div>
  );
}

const INPUT: React.CSSProperties = {
  width: '100%', boxSizing: 'border-box', padding: '8px 10px',
  border: '1px solid #e2e8f0', borderRadius: 6, fontSize: 13,
  color: '#1e293b', background: '#fff', outline: 'none',
};

function ActionBtn({ color, children, onClick }: {
  color: string; children: React.ReactNode; onClick: () => void;
}) {
  return (
    <button onClick={onClick} style={{
      marginRight: 4, padding: '3px 10px', fontSize: 12, fontWeight: 500,
      border: `1px solid ${color}33`, borderRadius: 5, cursor: 'pointer',
      color, background: `${color}14`, lineHeight: '1.6',
    }}>
      {children}
    </button>
  );
}

// ─────────── main page ───────────

/**
 * 地面站管理頁面.
 *
 * @author Jeff
 * @since 2026-03-06
 */
export default function GroundStationsPage() {
  const [stations, setStations]     = useState<GroundStation[]>([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [showForm, setShowForm]     = useState(false);
  const [editTarget, setEditTarget] = useState<GroundStation | null>(null);
  const [form, setForm]             = useState<Partial<GroundStationRequest>>({});
  const [sortKey, setSortKey]       = useState<SortKey>('id');
  const [sortDir, setSortDir]       = useState<SortDir>('asc');
  const [search, setSearch]         = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const res = await groundStationApi.getAll(0, 200);
      setStations(res.data.data.content);
    } catch {
      setError('載入地面站資料失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleSort = (key: SortKey) => {
    setSortDir(prev => (sortKey === key && prev === 'asc') ? 'desc' : 'asc');
    setSortKey(key);
  };

  const sorted = useMemo(() => {
    const q = search.trim().toLowerCase();
    const filtered = q
      ? stations.filter(s =>
          s.name.toLowerCase().includes(q) ||
          (s.code ?? '').toLowerCase().includes(q) ||
          s.frequencyBand.toLowerCase().includes(q))
      : stations;

    return [...filtered].sort((a, b) => {
      const av: unknown = a[sortKey as keyof GroundStation];
      const bv: unknown = b[sortKey as keyof GroundStation];
      const al = typeof av === 'string' ? av.toLowerCase() : (av ?? '');
      const bl = typeof bv === 'string' ? bv.toLowerCase() : (bv ?? '');
      if (al < bl) return sortDir === 'asc' ? -1 : 1;
      if (al > bl) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });
  }, [stations, sortKey, sortDir, search]);

  const openCreate = () => {
    setEditTarget(null);
    setForm({ setupTime: 300, teardownTime: 300, frequencyBand: 'X', minElevation: 5, enabled: true });
    setShowForm(true);
  };

  const openEdit = (gs: GroundStation) => {
    setEditTarget(gs);
    setForm({ ...gs });
    setShowForm(true);
  };

  const handleSave = async () => {
    try {
      if (editTarget) {
        await groundStationApi.update(editTarget.id, form as GroundStationRequest);
      } else {
        await groundStationApi.create(form as GroundStationRequest);
      }
      setShowForm(false);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? '儲存失敗';
      setError(msg);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('確定要刪除此地面站？')) return;
    try {
      await groundStationApi.delete(id);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? '刪除失敗';
      setError(msg);
    }
  };

  const handleToggle = async (gs: GroundStation) => {
    try {
      await groundStationApi.setEnabled(gs.id, !gs.enabled);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? '操作失敗';
      setError(msg);
    }
  };

  const f = (key: keyof GroundStationRequest, value: unknown) =>
    setForm(prev => ({ ...prev, [key]: value }));

  return (
    <div className="page-container">
      {/* ── Header ── */}
      <div className="page-header">
        <div>
          <h1 className="page-title">地面站管理</h1>
          <p className="page-subtitle">管理所有地面接收站資源，共 {stations.length} 座</p>
        </div>
        <button onClick={openCreate} className="btn btn-primary">+ 新增地面站</button>
      </div>

      {/* ── Error banner ── */}
      {error && (
        <div className="alert alert-danger">
          <span>⚠</span>
          <span>{error}</span>
          <button className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }} onClick={() => setError('')}>×</button>
        </div>
      )}

      {/* ── Search bar ── */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="card-body" style={{ paddingTop: 12, paddingBottom: 12 }}>
          <div className="search-bar">
            <div className="search-input-wrapper">
              <span className="search-icon">🔍</span>
              <input
                className="search-input"
                placeholder="搜尋名稱、代碼、頻段…"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            {search && (
              <button className="btn btn-ghost btn-sm" onClick={() => setSearch('')}>清除</button>
            )}
            <span style={{ marginLeft: 'auto', fontSize: 12, color: '#64748b' }}>
              顯示 {sorted.length} / {stations.length} 筆
            </span>
          </div>
        </div>
      </div>

      {/* ── Table ── */}
      {loading ? (
        <div className="loading-overlay"><div className="spinner" /><span>載入中…</span></div>
      ) : sorted.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📡</div>
          <div className="empty-state-title">尚無地面站資料</div>
          <div className="empty-state-description">點擊右上角「新增地面站」建立第一座</div>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <SortTh label="ID"        sk="id"            cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="名稱"      sk="name"          cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="代碼"      sk="code"          cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="頻段"      sk="frequencyBand" cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="座標"      sk="latitude"      cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="前置/回復" sk="setupTime"     cur={sortKey} dir={sortDir} onSort={handleSort} right />
                  <SortTh label="狀態"      sk="enabled"       cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <th style={{
                    padding: '11px 14px', fontSize: 12, fontWeight: 600, color: '#64748b',
                    background: '#f8fafc', borderBottom: '2px solid #e2e8f0',
                    textTransform: 'uppercase', letterSpacing: '0.04em',
                  }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {sorted.map((gs, idx) => {
                  const chip = BAND_CHIP[gs.frequencyBand] ?? BAND_CHIP.X;
                  const rowBg = idx % 2 === 0 ? '#ffffff' : '#f8fafc';
                  return (
                    <tr key={gs.id} style={{ background: rowBg }}>
                      <td style={{ padding: '11px 14px', color: '#94a3b8', fontSize: 12 }}>
                        {gs.id}
                      </td>
                      <td style={{ padding: '11px 14px', fontWeight: 600, color: '#0f172a' }}>
                        {gs.name}
                      </td>
                      <td style={{ padding: '11px 14px', fontFamily: 'monospace', fontSize: 12, color: '#475569' }}>
                        {gs.code ?? <span style={{ color: '#cbd5e1' }}>—</span>}
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        <span style={{
                          display: 'inline-block', padding: '2px 10px', borderRadius: 999,
                          fontSize: 11, fontWeight: 700, letterSpacing: '0.05em', ...chip,
                        }}>
                          {gs.frequencyBand}-Band
                        </span>
                      </td>
                      <td style={{ padding: '11px 14px', fontSize: 12, color: '#475569', fontVariantNumeric: 'tabular-nums' }}>
                        {gs.latitude.toFixed(3)}°N, {gs.longitude.toFixed(3)}°E
                      </td>
                      <td style={{ padding: '11px 14px', textAlign: 'right', fontSize: 12, color: '#475569', fontVariantNumeric: 'tabular-nums' }}>
                        {gs.setupTime}s / {gs.teardownTime}s
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        {gs.enabled ? (
                          <span style={{
                            display: 'inline-flex', alignItems: 'center', gap: 5,
                            padding: '3px 10px', borderRadius: 999, fontSize: 11, fontWeight: 700,
                            background: '#dcfce7', color: '#15803d', border: '1px solid #86efac',
                          }}>
                            <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#16a34a', flexShrink: 0 }} />
                            啟用
                          </span>
                        ) : (
                          <span style={{
                            display: 'inline-flex', alignItems: 'center', gap: 5,
                            padding: '3px 10px', borderRadius: 999, fontSize: 11, fontWeight: 700,
                            background: '#f1f5f9', color: '#64748b', border: '1px solid #e2e8f0',
                          }}>
                            <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#94a3b8', flexShrink: 0 }} />
                            停用
                          </span>
                        )}
                      </td>
                      <td style={{ padding: '11px 14px', whiteSpace: 'nowrap' }}>
                        <ActionBtn color="#3b82f6" onClick={() => openEdit(gs)}>編輯</ActionBtn>
                        <ActionBtn color={gs.enabled ? '#f59e0b' : '#10b981'} onClick={() => handleToggle(gs)}>
                          {gs.enabled ? '停用' : '啟用'}
                        </ActionBtn>
                        <ActionBtn color="#ef4444" onClick={() => handleDelete(gs.id)}>刪除</ActionBtn>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── Form Modal ── */}
      {showForm && (
        <div className="modal-backdrop" onClick={() => setShowForm(false)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">{editTarget ? '編輯地面站' : '新增地面站'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>×</button>
            </div>
            <div className="modal-body">
              <Field label="名稱 *">
                <input style={INPUT} placeholder="例：台灣主站"
                  value={form.name ?? ''} onChange={e => f('name', e.target.value)} />
              </Field>
              <Field label="代碼">
                <input style={INPUT} placeholder="例：GS-TW"
                  value={form.code ?? ''} onChange={e => f('code', e.target.value)} />
              </Field>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="經度 *">
                  <input style={INPUT} type="number" step="0.0001"
                    value={form.longitude ?? ''} onChange={e => f('longitude', parseFloat(e.target.value))} />
                </Field>
                <Field label="緯度 *">
                  <input style={INPUT} type="number" step="0.0001"
                    value={form.latitude ?? ''} onChange={e => f('latitude', parseFloat(e.target.value))} />
                </Field>
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="前置時間 (秒) *">
                  <input style={INPUT} type="number"
                    value={form.setupTime ?? 300} onChange={e => f('setupTime', parseInt(e.target.value))} />
                </Field>
                <Field label="回復時間 (秒) *">
                  <input style={INPUT} type="number"
                    value={form.teardownTime ?? 300} onChange={e => f('teardownTime', parseInt(e.target.value))} />
                </Field>
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="頻段 *">
                  <select style={INPUT} value={form.frequencyBand ?? 'X'}
                    onChange={e => f('frequencyBand', e.target.value as FrequencyBand)}>
                    {BANDS.map(b => <option key={b} value={b}>{b}-Band</option>)}
                  </select>
                </Field>
                <Field label="最低仰角 (°)">
                  <input style={INPUT} type="number" step="0.5"
                    value={form.minElevation ?? 5} onChange={e => f('minElevation', parseFloat(e.target.value))} />
                </Field>
              </div>
              <Field label="描述">
                <textarea style={{ ...INPUT, height: 72, resize: 'vertical' } as React.CSSProperties}
                  value={form.description ?? ''} onChange={e => f('description', e.target.value)} />
              </Field>
              <Field label="狀態">
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: '#374151', cursor: 'pointer' }}>
                  <input type="checkbox" checked={form.enabled ?? true}
                    onChange={e => f('enabled', e.target.checked)} />
                  啟用此地面站
                </label>
              </Field>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowForm(false)}>取消</button>
              <button className="btn btn-primary" onClick={handleSave}>儲存</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
