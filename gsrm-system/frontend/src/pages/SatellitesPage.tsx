import React, { useEffect, useMemo, useState } from 'react';
import { satelliteApi } from '../api/satelliteApi';
import type { Satellite, SatelliteCreateRequest, FrequencyBand } from '../types';

// ─────────── constants ───────────

const BANDS: FrequencyBand[] = ['X', 'S', 'XS'];

type SortKey = 'id' | 'name' | 'company' | 'frequencyBand' | 'priorityWeight' | 'minDailyPasses' | 'isEmergency' | 'enabled';
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

function PriorityBar({ value }: { value: number }) {
  const pct = Math.min(Math.max(value, 0), 100);
  const color = pct >= 75 ? '#ef4444' : pct >= 50 ? '#f59e0b' : '#3b82f6';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <div style={{ flex: 1, height: 5, background: '#e2e8f0', borderRadius: 999, overflow: 'hidden' }}>
        <div style={{ width: `${pct}%`, height: '100%', background: color, borderRadius: 999 }} />
      </div>
      <span style={{ fontSize: 11, color: '#64748b', minWidth: 24, textAlign: 'right' }}>{pct}</span>
    </div>
  );
}

// ─────────── main page ───────────

/**
 * 衛星管理頁面.
 *
 * @author Jeff
 * @since 2026-03-06
 */
export default function SatellitesPage() {
  const [satellites, setSatellites] = useState<Satellite[]>([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [showForm, setShowForm]     = useState(false);
  const [editTarget, setEditTarget] = useState<Satellite | null>(null);
  const [form, setForm]             = useState<Partial<SatelliteCreateRequest>>({});
  const [sortKey, setSortKey]       = useState<SortKey>('id');
  const [sortDir, setSortDir]       = useState<SortDir>('asc');
  const [search, setSearch]         = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const res = await satelliteApi.getAll(0, 200);
      setSatellites(res.data.data.content);
    } catch {
      setError('載入衛星資料失敗');
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
      ? satellites.filter(s =>
          s.name.toLowerCase().includes(q) ||
          (s.code ?? '').toLowerCase().includes(q) ||
          (s.company ?? '').toLowerCase().includes(q) ||
          s.frequencyBand.toLowerCase().includes(q))
      : satellites;

    return [...filtered].sort((a, b) => {
      const av: unknown = a[sortKey as keyof Satellite];
      const bv: unknown = b[sortKey as keyof Satellite];
      const al = typeof av === 'string' ? av.toLowerCase() : (av ?? '');
      const bl = typeof bv === 'string' ? bv.toLowerCase() : (bv ?? '');
      if (al < bl) return sortDir === 'asc' ? -1 : 1;
      if (al > bl) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });
  }, [satellites, sortKey, sortDir, search]);

  const openCreate = () => {
    setEditTarget(null);
    setForm({ minDailyPasses: 1, minPassDuration: 60, priorityWeight: 50, frequencyBand: 'X', isEmergency: false, enabled: true });
    setShowForm(true);
  };

  const openEdit = (sat: Satellite) => {
    setEditTarget(sat);
    setForm({ ...sat });
    setShowForm(true);
  };

  const handleSave = async () => {
    try {
      if (editTarget) {
        await satelliteApi.update(editTarget.id, form as SatelliteCreateRequest);
      } else {
        await satelliteApi.create(form as SatelliteCreateRequest);
      }
      setShowForm(false);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? '儲存失敗';
      setError(msg);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('確定要刪除此衛星？')) return;
    try {
      await satelliteApi.delete(id);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? '刪除失敗';
      setError(msg);
    }
  };

  const f = (key: keyof SatelliteCreateRequest, value: unknown) =>
    setForm(prev => ({ ...prev, [key]: value }));

  return (
    <div className="page-container">
      {/* ── Header ── */}
      <div className="page-header">
        <div>
          <h1 className="page-title">衛星管理</h1>
          <p className="page-subtitle">管理所有衛星任務資源，共 {satellites.length} 顆</p>
        </div>
        <button onClick={openCreate} className="btn btn-primary">+ 新增衛星</button>
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
                placeholder="搜尋名稱、代碼、公司、頻段…"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            {search && (
              <button className="btn btn-ghost btn-sm" onClick={() => setSearch('')}>清除</button>
            )}
            <span style={{ marginLeft: 'auto', fontSize: 12, color: '#64748b' }}>
              顯示 {sorted.length} / {satellites.length} 筆
            </span>
          </div>
        </div>
      </div>

      {/* ── Table ── */}
      {loading ? (
        <div className="loading-overlay"><div className="spinner" /><span>載入中…</span></div>
      ) : sorted.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🛰</div>
          <div className="empty-state-title">尚無衛星資料</div>
          <div className="empty-state-description">點擊右上角「新增衛星」建立第一顆</div>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <SortTh label="ID"        sk="id"             cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="名稱"      sk="name"           cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="公司"      sk="company"        cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="頻段"      sk="frequencyBand"  cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="優先權"    sk="priorityWeight" cur={sortKey} dir={sortDir} onSort={handleSort} right />
                  <SortTh label="Pass/日"  sk="minDailyPasses" cur={sortKey} dir={sortDir} onSort={handleSort} right />
                  <SortTh label="緊急"      sk="isEmergency"    cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <SortTh label="狀態"      sk="enabled"        cur={sortKey} dir={sortDir} onSort={handleSort} />
                  <th style={{
                    padding: '11px 14px', fontSize: 12, fontWeight: 600, color: '#64748b',
                    background: '#f8fafc', borderBottom: '2px solid #e2e8f0',
                    textTransform: 'uppercase', letterSpacing: '0.04em',
                  }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {sorted.map((sat, idx) => {
                  const chip = BAND_CHIP[sat.frequencyBand] ?? BAND_CHIP.X;
                  const rowBg = idx % 2 === 0 ? '#ffffff' : '#f8fafc';
                  return (
                    <tr key={sat.id} style={{ background: rowBg }}>
                      <td style={{ padding: '11px 14px', color: '#94a3b8', fontSize: 12 }}>
                        {sat.id}
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        <div style={{ fontWeight: 600, color: '#0f172a', fontSize: 13 }}>{sat.name}</div>
                        {sat.code && (
                          <div style={{ fontSize: 11, color: '#94a3b8', fontFamily: 'monospace', marginTop: 2 }}>{sat.code}</div>
                        )}
                      </td>
                      <td style={{ padding: '11px 14px', fontSize: 12, color: '#475569' }}>
                        {sat.company ?? <span style={{ color: '#cbd5e1' }}>—</span>}
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        <span style={{
                          display: 'inline-block', padding: '2px 10px', borderRadius: 999,
                          fontSize: 11, fontWeight: 700, letterSpacing: '0.05em', ...chip,
                        }}>
                          {sat.frequencyBand}-Band
                        </span>
                      </td>
                      <td style={{ padding: '11px 14px', minWidth: 110 }}>
                        <PriorityBar value={sat.priorityWeight} />
                      </td>
                      <td style={{ padding: '11px 14px', textAlign: 'right', fontSize: 12, color: '#475569', fontVariantNumeric: 'tabular-nums' }}>
                        {sat.minDailyPasses}
                        <span style={{ color: '#94a3b8', marginLeft: 2, fontSize: 11 }}>次</span>
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        {sat.isEmergency ? (
                          <span style={{
                            display: 'inline-flex', alignItems: 'center', gap: 4,
                            padding: '2px 8px', borderRadius: 999, fontSize: 11, fontWeight: 700,
                            background: '#fef2f2', color: '#dc2626', border: '1px solid #fca5a5',
                          }}>
                            ⚡ 緊急
                          </span>
                        ) : (
                          <span style={{ color: '#cbd5e1', fontSize: 13 }}>—</span>
                        )}
                      </td>
                      <td style={{ padding: '11px 14px' }}>
                        {sat.enabled ? (
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
                        <ActionBtn color="#3b82f6" onClick={() => openEdit(sat)}>編輯</ActionBtn>
                        <ActionBtn color="#ef4444" onClick={() => handleDelete(sat.id)}>刪除</ActionBtn>
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
              <h3 className="modal-title">{editTarget ? '編輯衛星' : '新增衛星'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>×</button>
            </div>
            <div className="modal-body">
              <Field label="名稱 *">
                <input style={INPUT} placeholder="例：福衛七號"
                  value={form.name ?? ''} onChange={e => f('name', e.target.value)} />
              </Field>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="代碼">
                  <input style={INPUT} placeholder="例：SAT-001"
                    value={form.code ?? ''} onChange={e => f('code', e.target.value)} />
                </Field>
                <Field label="公司 / 機構">
                  <input style={INPUT} placeholder="例：國家太空中心"
                    value={form.company ?? ''} onChange={e => f('company', e.target.value)} />
                </Field>
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="頻段 *">
                  <select style={INPUT} value={form.frequencyBand ?? 'X'}
                    onChange={e => f('frequencyBand', e.target.value as FrequencyBand)}>
                    {BANDS.map(b => <option key={b} value={b}>{b}-Band</option>)}
                  </select>
                </Field>
                <Field label="優先權 (1–100) *">
                  <input style={INPUT} type="number" min={1} max={100}
                    value={form.priorityWeight ?? 50}
                    onChange={e => f('priorityWeight', parseInt(e.target.value))} />
                </Field>
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <Field label="最低 Pass 數 / 日 *">
                  <input style={INPUT} type="number" min={0}
                    value={form.minDailyPasses ?? 1}
                    onChange={e => f('minDailyPasses', parseInt(e.target.value))} />
                </Field>
                <Field label="最短 Pass 秒數 *">
                  <input style={INPUT} type="number" min={1}
                    value={form.minPassDuration ?? 60}
                    onChange={e => f('minPassDuration', parseInt(e.target.value))} />
                </Field>
              </div>
              <Field label="描述">
                <textarea style={{ ...INPUT, height: 68, resize: 'vertical' } as React.CSSProperties}
                  value={form.description ?? ''} onChange={e => f('description', e.target.value)} />
              </Field>
              <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: '#374151', cursor: 'pointer' }}>
                  <input type="checkbox" checked={form.isEmergency ?? false}
                    onChange={e => f('isEmergency', e.target.checked)} />
                  <span style={{ color: '#dc2626', fontWeight: 500 }}>⚡ 緊急任務衛星</span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: '#374151', cursor: 'pointer' }}>
                  <input type="checkbox" checked={form.enabled ?? true}
                    onChange={e => f('enabled', e.target.checked)} />
                  啟用此衛星
                </label>
              </div>
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
