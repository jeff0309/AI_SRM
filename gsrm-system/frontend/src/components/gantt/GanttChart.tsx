import React, { useMemo, useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { scheduleApi } from '../../api/scheduleApi';
import { satelliteApi } from '../../api/satelliteApi';
import { groundStationApi } from '../../api/groundStationApi';
import type {
  GanttChartData,
  GroundStationRow,
  PassItem,
  ManualPassRequest,
  FrequencyBand,
} from '../../types';

// ─────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────

const ROW_HEIGHT     = 56;   // px per ground-station row
const HEADER_HEIGHT  = 48;   // px for time-ruler
const MIN_PX_PER_MIN = 1.2;  // minimum pixels per minute
const MAX_PX_PER_MIN = 8;    // maximum pixels per minute (zoomed in)
const PASS_BAR_H     = 28;   // height of a pass bar within a row

const PASS_COLOR: Record<string, string> = {
  SCHEDULED:  '#34a853',
  SHORTENED:  '#fbbc04',
  REJECTED:   '#ea4335',
  FORCED:     '#9c27b0',
  PENDING:    '#9aa0a6',
};

const PASS_LABEL: Record<string, string> = {
  SCHEDULED: '已排程',
  SHORTENED: '已縮短',
  REJECTED:  '已拒絕',
  FORCED:    '強制',
  PENDING:   '待定',
};

// ─────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────

/**
 * Normalize backend datetime strings to standard ISO 8601 with T separator.
 * Backend may return "2026-03-10 08:00:00" (space) or "2026-03-10T08:00:00" (T).
 * Without normalization, browsers may parse the space-separated form as UTC
 * instead of local time, causing the time axis to shift by the UTC offset.
 */
function normalizeDate(s: string): Date {
  return new Date(s.replace(' ', 'T'));
}

function toMinutes(iso: string, epochStart: number): number {
  return (normalizeDate(iso).getTime() - epochStart) / 60_000;
}

function fmtHHMM(d: Date): string {
  return d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function fmtDatetime(iso: string): string {
  if (!iso) return '—';
  return normalizeDate(iso).toLocaleString('zh-TW', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
}

function toDatetimeLocal(iso: string): string {
  if (!iso) return '';
  // Convert to local ISO string for datetime-local input (no timezone suffix)
  const d = normalizeDate(iso);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

// ─────────────────────────────────────────────────────────────
// Manual Pass Form
// ─────────────────────────────────────────────────────────────

interface ManualPassFormProps {
  sessionId: number;
  data: GanttChartData;
  onSuccess: () => void;
  onCancel: () => void;
}

function ManualPassForm({ sessionId, data, onSuccess, onCancel }: ManualPassFormProps) {
  const defaultAos = toDatetimeLocal(data.scheduleStartTime);
  const defaultLos = toDatetimeLocal(data.scheduleStartTime);

  const [form, setForm] = useState<ManualPassRequest>({
    sessionId,
    satelliteId:    0,
    groundStationId: 0,
    frequencyBand:  'X',
    aos: defaultAos,
    los: defaultLos,
    notes: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const set = (field: keyof ManualPassRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
      setForm(prev => ({ ...prev, [field]: e.target.value }));

  // Load enabled satellites and ground stations from API directly
  const { data: satList = [] } = useQuery({
    queryKey: ['satellites-enabled'],
    queryFn: () => satelliteApi.getEnabled().then(r => r.data.data),
  });
  const { data: gsList = [] } = useQuery({
    queryKey: ['ground-stations-enabled'],
    queryFn: () => groundStationApi.getEnabled().then(r => r.data.data),
  });

  const qc = useQueryClient();
  const mut = useMutation({
    mutationFn: (req: ManualPassRequest) => scheduleApi.addManualPass({
      ...req,
      satelliteId:     Number(req.satelliteId),
      groundStationId: Number(req.groundStationId),
      aos: new Date(req.aos).toISOString(),
      los: new Date(req.los).toISOString(),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['gantt', sessionId] });
      onSuccess();
    },
  });

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.satelliteId)    errs.satelliteId    = '請選擇衛星';
    if (!form.groundStationId) errs.groundStationId = '請選擇地面站';
    if (!form.aos)            errs.aos            = '請輸入 AOS 時間';
    if (!form.los)            errs.los            = '請輸入 LOS 時間';
    if (form.aos && form.los && form.aos >= form.los) errs.los = 'LOS 必須晚於 AOS';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    mut.mutate(form);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div className="form-group">
          <label className="form-label required">衛星</label>
          <select
            className={`form-control ${errors.satelliteId ? 'is-invalid' : ''}`}
            value={form.satelliteId}
            onChange={set('satelliteId')}
          >
            <option value={0}>— 選擇衛星 —</option>
            {satList.map(s => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
          {errors.satelliteId && <span className="form-error">{errors.satelliteId}</span>}
        </div>
        <div className="form-group">
          <label className="form-label required">地面站</label>
          <select
            className={`form-control ${errors.groundStationId ? 'is-invalid' : ''}`}
            value={form.groundStationId}
            onChange={set('groundStationId')}
          >
            <option value={0}>— 選擇地面站 —</option>
            {gsList.map(gs => (
              <option key={gs.id} value={gs.id}>
                {gs.name}
              </option>
            ))}
          </select>
          {errors.groundStationId && <span className="form-error">{errors.groundStationId}</span>}
        </div>
        <div className="form-group">
          <label className="form-label required">AOS（衛星升空）</label>
          <input
            type="datetime-local"
            className={`form-control ${errors.aos ? 'is-invalid' : ''}`}
            value={form.aos}
            onChange={set('aos')}
          />
          {errors.aos && <span className="form-error">{errors.aos}</span>}
        </div>
        <div className="form-group">
          <label className="form-label required">LOS（衛星消失）</label>
          <input
            type="datetime-local"
            className={`form-control ${errors.los ? 'is-invalid' : ''}`}
            value={form.los}
            onChange={set('los')}
          />
          {errors.los && <span className="form-error">{errors.los}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">頻段</label>
          <select className="form-control" value={form.frequencyBand} onChange={set('frequencyBand')}>
            {(['X', 'S', 'XS'] as FrequencyBand[]).map(b => (
              <option key={b} value={b}>{b}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">備注</label>
          <input
            className="form-control"
            value={form.notes}
            onChange={set('notes')}
            placeholder="選填"
          />
        </div>
      </div>

      {mut.isError && (
        <div className="alert alert-danger" style={{ marginTop: 8 }}>
          新增失敗：{(mut.error as Error).message}
        </div>
      )}

      <div className="modal-footer" style={{ padding: 0, paddingTop: 12 }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={mut.isPending}>
          取消
        </button>
        <button type="submit" className="btn btn-primary" disabled={mut.isPending}>
          {mut.isPending ? <><span className="spinner spinner-sm" /> 新增中…</> : '新增 Pass'}
        </button>
      </div>
    </form>
  );
}

// ─────────────────────────────────────────────────────────────
// Pass Tooltip
// ─────────────────────────────────────────────────────────────

interface PassTooltipProps {
  pass: PassItem;
  x: number;
  y: number;
}

function PassTooltip({ pass, x, y }: PassTooltipProps) {
  const duration = pass.durationSeconds
    ? `${Math.floor(pass.durationSeconds / 60)}m ${pass.durationSeconds % 60}s`
    : '—';
  const shortened = pass.shortenedSeconds
    ? `-${Math.floor(pass.shortenedSeconds / 60)}m ${pass.shortenedSeconds % 60}s`
    : null;

  return (
    <div
      style={{
        position: 'fixed',
        left:     Math.min(x + 12, window.innerWidth - 280),
        top:      y - 10,
        background: '#202124',
        color: '#fff',
        borderRadius: 8,
        padding: '10px 14px',
        fontSize: 12,
        zIndex: 9999,
        minWidth: 240,
        boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
        pointerEvents: 'none',
      }}
    >
      <div style={{ fontWeight: 600, marginBottom: 6, fontSize: 13 }}>{pass.satelliteName}</div>
      <div style={{ display: 'grid', gridTemplateColumns: '80px 1fr', gap: '3px 8px', lineHeight: 1.6 }}>
        <span style={{ color: '#9aa0a6' }}>狀態</span>
        <span style={{ color: PASS_COLOR[pass.status] ?? '#fff' }}>
          {PASS_LABEL[pass.status] ?? pass.status}
        </span>
        <span style={{ color: '#9aa0a6' }}>AOS</span>
        <span>{fmtDatetime(pass.scheduledAos)}</span>
        <span style={{ color: '#9aa0a6' }}>LOS</span>
        <span>{fmtDatetime(pass.scheduledLos)}</span>
        <span style={{ color: '#9aa0a6' }}>時長</span>
        <span>{duration}</span>
        {shortened && <><span style={{ color: '#9aa0a6' }}>縮短</span><span style={{ color: '#fbbc04' }}>{shortened}</span></>}
        {pass.isForced && <><span style={{ color: '#9aa0a6' }}>強制</span><span style={{ color: '#9c27b0' }}>是</span></>}
        {pass.notes && <><span style={{ color: '#9aa0a6' }}>備注</span><span>{pass.notes}</span></>}
      </div>
    </div>
  );
}

/**
 * 待排程 (已拒絕) Pass 面板
 */
interface UnscheduledPassPanelProps {
  data: GanttChartData;
  onDragStart: (e: React.DragEvent, pass: PassItem) => void;
  onDragEnd: () => void;
}

function UnscheduledPassPanel({ data, onDragStart, onDragEnd }: UnscheduledPassPanelProps) {
  const rejectedPasses = useMemo(() => {
    const allRejected: PassItem[] = [];
    data.groundStations.forEach(gs => {
      gs.passes.forEach(p => {
        if (p.status === 'REJECTED') {
          allRejected.push(p);
        }
      });
    });

    // 如果沒有數據，提供幾筆假資料供測試
    if (allRejected.length === 0) {
      const baseTime = normalizeDate(data.scheduleStartTime).getTime();
      return [
        {
          passId: 9001,
          satelliteId: 101,
          satelliteName: 'MOCK-SAT-01',
          frequencyBand: 'X',
          originalAos: new Date(baseTime + 10 * 60_000).toISOString(),
          originalLos: new Date(baseTime + 25 * 60_000).toISOString(),
          scheduledAos: '', scheduledLos: '',
          status: 'REJECTED', isAllowed: false, isForced: false,
          notes: '頻段衝突 (模擬資料)',
        },
        {
          passId: 9002,
          satelliteId: 102,
          satelliteName: 'MOCK-SAT-02',
          frequencyBand: 'S',
          originalAos: new Date(baseTime + 45 * 60_000).toISOString(),
          originalLos: new Date(baseTime + 65 * 60_000).toISOString(),
          scheduledAos: '', scheduledLos: '',
          status: 'REJECTED', isAllowed: false, isForced: false,
          notes: '地面站維護 (模擬資料)',
        },
        {
          passId: 9003,
          satelliteId: 103,
          satelliteName: 'MOCK-SAT-03',
          frequencyBand: 'X',
          originalAos: new Date(baseTime + 120 * 60_000).toISOString(),
          originalLos: new Date(baseTime + 140 * 60_000).toISOString(),
          scheduledAos: '', scheduledLos: '',
          status: 'REJECTED', isAllowed: false, isForced: false,
          notes: '優先級不足 (模擬資料)',
        },
      ] as PassItem[];
    }

    // 按 AOS 排序
    return allRejected.sort((a, b) => normalizeDate(a.originalAos).getTime() - normalizeDate(b.originalAos).getTime());
  }, [data]);

  return (
    <div style={{
      width: 260,
      background: '#fff',
      borderLeft: '1px solid #dadce0',
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
    }}>
      <div style={{
        padding: '12px 16px',
        borderBottom: '1px solid #dadce0',
        fontWeight: 600,
        fontSize: 14,
        background: '#f8f9fa',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <span>待排程 Pass ({rejectedPasses.length})</span>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: 12 }}>
        {rejectedPasses.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#9aa0a6', marginTop: 40, fontSize: 13 }}>
            無待排程數據
          </div>
        ) : (
          rejectedPasses.map(pass => (
            <div
              key={pass.passId}
              draggable
              onDragStart={(e) => onDragStart(e, pass)}
              onDragEnd={onDragEnd}
              style={{
                padding: '10px 12px',
                border: '1px solid #dadce0',
                borderRadius: 6,
                marginBottom: 8,
                cursor: 'grab',
                background: '#fff',
                transition: 'all 0.2s',
              }}
              onMouseEnter={e => e.currentTarget.style.borderColor = '#1a73e8'}
              onMouseLeave={e => e.currentTarget.style.borderColor = '#dadce0'}
            >
              <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>{pass.satelliteName}</div>
              <div style={{ fontSize: 11, color: '#5f6368', display: 'grid', gridTemplateColumns: '1fr 1fr' }}>
                <span>AOS: {fmtHHMM(normalizeDate(pass.originalAos))}</span>
                <span>LOS: {fmtHHMM(normalizeDate(pass.originalLos))}</span>
              </div>
              <div style={{ fontSize: 11, color: '#ea4335', marginTop: 4, fontStyle: 'italic' }}>
                {pass.notes || '排程失敗'}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Gantt Row
// ─────────────────────────────────────────────────────────────

interface GanttRowProps {
  row:        GroundStationRow;
  epochStart: number;
  totalMin:   number;
  pxPerMin:   number;
  onDeletePass: (passId: number) => void;
  onDropPass: (gsId: number, requestId: number, dropTime: string) => void;
  draggedPass:  PassItem | null;
  canEdit:    boolean;
}

function GanttRow({ row, epochStart, totalMin, pxPerMin, onDeletePass, onDropPass, draggedPass, canEdit }: GanttRowProps) {
  const [tooltip, setTooltip] = useState<{ pass: PassItem; x: number; y: number } | null>(null);
  const [dragOverMin, setDragOverMin] = useState<number | null>(null);

  const totalWidth = totalMin * pxPerMin;

  return (
    <div style={{ display: 'flex', borderBottom: '1px solid #dadce0', position: 'relative' }}>
      {/* Left label */}
      <div
        style={{
          width: 180,
          minWidth: 180,
          padding: '8px 12px',
          background: '#f8f9fa',
          borderRight: '1px solid #dadce0',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
        }}
      >
        <div style={{ fontWeight: 600, fontSize: 13, color: '#202124' }}>{row.groundStationName}</div>
        <div style={{ fontSize: 11, color: '#5f6368' }}>{row.frequencyBand} 頻段</div>
        <div style={{ fontSize: 11, color: '#5f6368' }}>{row.passes.length} Pass</div>
      </div>

      {/* Timeline area */}
      <div
        style={{
          position: 'relative',
          height: ROW_HEIGHT,
          width: totalWidth,
          overflow: 'hidden',
          flexShrink: 0,
          background: canEdit ? 'rgba(26, 115, 232, 0.02)' : 'transparent',
        }}
        onDragOver={(e) => {
          if (!canEdit) return;
          e.preventDefault();
          const rect = e.currentTarget.getBoundingClientRect();
          const x = e.clientX - rect.left;
          setDragOverMin(x / pxPerMin);
          e.dataTransfer.dropEffect = 'move';
        }}
        onDragLeave={() => setDragOverMin(null)}
        onDrop={(e) => {
          if (!canEdit) return;
          e.preventDefault();
          setDragOverMin(null);
          const rect = e.currentTarget.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const dropMin = x / pxPerMin;
          const dropTime = new Date(epochStart + dropMin * 60_000).toISOString();
          
          const passData = JSON.parse(e.dataTransfer.getData('application/json'));
          onDropPass(row.groundStationId, passData.requestId || passData.passId, dropTime);
        }}
      >
        {/* Shadow preview when dragging */}
        {dragOverMin !== null && draggedPass && (
          <div
            style={{
              position: 'absolute',
              left: dragOverMin * pxPerMin,
              width: ((normalizeDate(draggedPass.originalLos).getTime() - normalizeDate(draggedPass.originalAos).getTime()) / 60_000) * pxPerMin,
              top: (ROW_HEIGHT - PASS_BAR_H) / 2,
              height: PASS_BAR_H,
              background: PASS_COLOR.FORCED,
              opacity: 0.4,
              borderRadius: 4,
              border: '2px dashed #9c27b0',
              pointerEvents: 'none',
              zIndex: 10,
            }}
          />
        )}
        {/* Unavailability blocks */}
        {row.unavailabilities.map(u => {
          const left = toMinutes(u.startTime, epochStart) * pxPerMin;
          const width = (toMinutes(u.endTime, epochStart) - toMinutes(u.startTime, epochStart)) * pxPerMin;
          if (width <= 0) return null;
          return (
            <div
              key={u.id}
              title={`維護：${u.reason ?? '無說明'}`}
              style={{
                position: 'absolute',
                left, width,
                top: 0, bottom: 0,
                background: 'repeating-linear-gradient(45deg, #f5c6c3, #f5c6c3 4px, #fce8e6 4px, #fce8e6 8px)',
                opacity: 0.7,
                borderLeft: '2px solid #ea4335',
              }}
            />
          );
        })}

        {/* Pass bars */}
        {row.passes.map(pass => {
          const left  = toMinutes(pass.scheduledAos, epochStart) * pxPerMin;
          const right = toMinutes(pass.scheduledLos, epochStart) * pxPerMin;
          const width = Math.max(right - left, 4);
          const color = PASS_COLOR[pass.status] ?? '#9aa0a6';
          const top   = (ROW_HEIGHT - PASS_BAR_H) / 2;

          return (
            <div
              key={pass.passId}
              style={{
                position: 'absolute',
                left, width,
                top, height: PASS_BAR_H,
                background: color,
                borderRadius: 4,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                paddingLeft: 4,
                overflow: 'hidden',
                boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
              }}
              onMouseEnter={e => setTooltip({ pass, x: e.clientX, y: e.clientY })}
              onMouseMove={e => setTooltip({ pass, x: e.clientX, y: e.clientY })}
              onMouseLeave={() => setTooltip(null)}
            >
              {width > 40 && (
                <span style={{ fontSize: 10, color: '#fff', whiteSpace: 'nowrap', overflow: 'hidden' }}>
                  {pass.satelliteName}
                </span>
              )}
              {canEdit && width > 24 && (
                <button
                  style={{
                    marginLeft: 'auto',
                    marginRight: 2,
                    background: 'rgba(0,0,0,0.25)',
                    border: 'none',
                    borderRadius: 3,
                    color: '#fff',
                    cursor: 'pointer',
                    fontSize: 10,
                    lineHeight: 1,
                    padding: '1px 3px',
                    flexShrink: 0,
                  }}
                  title="刪除此 Pass"
                  onMouseDown={e => { e.stopPropagation(); onDeletePass(pass.passId); }}
                >
                  ×
                </button>
              )}
            </div>
          );
        })}
      </div>

      {/* Tooltip */}
      {tooltip && <PassTooltip pass={tooltip.pass} x={tooltip.x} y={tooltip.y} />}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Time Ruler
// ─────────────────────────────────────────────────────────────

interface TimeRulerProps {
  epochStart: number;
  totalMin:   number;
  pxPerMin:   number;
}

function TimeRuler({ epochStart, totalMin, pxPerMin }: TimeRulerProps) {
  const totalWidth = totalMin * pxPerMin;

  // Determine tick interval based on zoom level
  const tickIntervalMin = pxPerMin < 2 ? 60 : pxPerMin < 4 ? 30 : 15;

  const multiDay = totalMin > 24 * 60; // session spans more than 1 day

  const ticks: { label: string; subLabel: string; x: number; isDayStart: boolean }[] = [];
  for (let m = 0; m <= totalMin; m += tickIntervalMin) {
    const d = new Date(epochStart + m * 60_000);
    const hhmm = fmtHHMM(d);
    const isNewDay = hhmm === '00:00' || m === 0;
    const dateLabel = multiDay && isNewDay
      ? d.toLocaleDateString('zh-TW', { month: '2-digit', day: '2-digit' })
      : '';
    ticks.push({ label: hhmm, subLabel: dateLabel, x: m * pxPerMin, isDayStart: isNewDay && m > 0 });
  }

  return (
    <div
      style={{
        display: 'flex',
        borderBottom: '2px solid #dadce0',
        background: '#f0f2f5',
        position: 'sticky',
        top: 0,
        zIndex: 10,
      }}
    >
      {/* Spacer for left label column */}
      <div style={{ width: 180, minWidth: 180, borderRight: '1px solid #dadce0', flexShrink: 0 }}>
        {/* Show session start date in header corner */}
        <div style={{ fontSize: 10, color: '#5f6368', padding: '4px 6px', textAlign: 'center', lineHeight: 1.4 }}>
          {new Date(epochStart).toLocaleDateString('zh-TW', { month: '2-digit', day: '2-digit' })}
          <br />
          {fmtHHMM(new Date(epochStart))}
        </div>
      </div>
      {/* Ruler */}
      <div style={{ position: 'relative', height: HEADER_HEIGHT, width: totalWidth, flexShrink: 0 }}>
        {ticks.map(t => (
          <div
            key={t.x}
            style={{
              position: 'absolute',
              left: t.x,
              top: 0,
              height: '100%',
              borderLeft: t.isDayStart ? '2px solid #adb5bd' : '1px solid #dadce0',
              paddingLeft: 4,
              fontSize: 11,
              color: t.isDayStart ? '#202124' : '#5f6368',
              fontWeight: t.isDayStart ? 600 : 400,
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'flex-end',
              paddingBottom: 4,
              whiteSpace: 'nowrap',
            }}
          >
            {t.subLabel && <span style={{ fontSize: 10, color: '#1a73e8' }}>{t.subLabel}</span>}
            {t.label}
          </div>
        ))}
        {/* Current-time line */}
        {(() => {
          const nowMin = (Date.now() - epochStart) / 60_000;
          if (nowMin < 0 || nowMin > totalMin) return null;
          return (
            <div
              style={{
                position: 'absolute',
                left: nowMin * pxPerMin,
                top: 0,
                bottom: 0,
                width: 2,
                background: '#ea4335',
                zIndex: 5,
              }}
            />
          );
        })()}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Legend
// ─────────────────────────────────────────────────────────────

function Legend() {
  return (
    <div style={{ display: 'flex', gap: 16, alignItems: 'center', flexWrap: 'wrap', fontSize: 12 }}>
      {Object.entries(PASS_LABEL).map(([status, label]) => (
        <div key={status} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <div style={{ width: 14, height: 14, borderRadius: 3, background: PASS_COLOR[status] }} />
          <span style={{ color: '#5f6368' }}>{label}</span>
        </div>
      ))}
      <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
        <div style={{
          width: 14, height: 14, borderRadius: 3,
          background: 'repeating-linear-gradient(45deg, #f5c6c3, #f5c6c3 3px, #fce8e6 3px, #fce8e6 6px)',
          border: '1px solid #ea4335',
        }} />
        <span style={{ color: '#5f6368' }}>維護停機</span>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// GanttChart (main export)
// ─────────────────────────────────────────────────────────────

interface GanttChartProps {
  sessionId: number;
}

export default function GanttChart({ sessionId }: GanttChartProps) {
  const qc = useQueryClient();

  // zoom state: px per minute
  const [pxPerMin, setPxPerMin]      = useState(2.5);
  const [showAddForm, setShowAddForm] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [filterStation, setFilterStation] = useState<number>(0);

  const ganttQuery = useQuery({
    queryKey: ['gantt', sessionId],
    queryFn:  () => scheduleApi.getGanttData(sessionId).then(r => r.data.data),
  });

  const deleteMut = useMutation({
    mutationFn: (passId: number) => scheduleApi.removePass(passId),
    onSuccess:  () => qc.invalidateQueries({ queryKey: ['gantt', sessionId] }),
  });

  const handleDeletePass = useCallback((passId: number) => {
    if (!window.confirm('確定要刪除此 Pass？')) return;
    deleteMut.mutate(passId);
  }, [deleteMut]);

  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [draggedPass, setDraggedPass] = useState<PassItem | null>(null);

  const handleDragStart = (e: React.DragEvent, pass: PassItem) => {
    setDraggedPass(pass);
    e.dataTransfer.setData('application/json', JSON.stringify(pass));
    e.dataTransfer.effectAllowed = 'move';
    
    // Set a ghost image if needed, or just let browser handle it
  };

  const handleDragEnd = () => {
    setDraggedPass(null);
  };

  const handleDropPass = async (gsId: number, requestId: number, dropTime: string) => {
    // 獲取原始 Pass 資訊以計算持續時間
    const pass = data?.groundStations.flatMap(gs => gs.passes).find(p => p.passId === requestId);
    if (!pass) return;

    const durationMs = normalizeDate(pass.originalLos).getTime() - normalizeDate(pass.originalAos).getTime();
    const end = new Date(new Date(dropTime).getTime() + durationMs).toISOString();

    // 1. 驗證
    try {
      const valRes = await scheduleApi.validatePass({
        sessionId,
        requestId,
        groundStationId: gsId,
        aos: dropTime,
        los: end
      });

      if (valRes.data.data.isConflict) {
        alert(`無法排入：${valRes.data.data.message}`);
        return;
      }

      if (!window.confirm(`確定要將此 Pass 排入地面站 ${data?.groundStations.find(g => g.groundStationId === gsId)?.groundStationName}？`)) {
        return;
      }

      // 2. 執行強制排入
      await scheduleApi.addManualPassFromRequest(requestId);
      qc.invalidateQueries({ queryKey: ['gantt', sessionId] });
    } catch (err) {
      alert(`操作失敗：${(err as Error).message}`);
    }
  };

  // ── Derived data ──
  const data: GanttChartData | undefined = ganttQuery.data;

  const { epochStart, totalMin } = useMemo(() => {
    if (!data) return { epochStart: Date.now(), totalMin: 60 };
    const start = normalizeDate(data.scheduleStartTime).getTime();
    const end   = normalizeDate(data.scheduleEndTime).getTime();
    return {
      epochStart: start,
      totalMin:   Math.ceil((end - start) / 60_000),
    };
  }, [data]);

  const filteredRows: GroundStationRow[] = useMemo(() => {
    if (!data) return [];
    return data.groundStations
      .filter(gs => filterStation === 0 || gs.groundStationId === filterStation)
      .map(gs => ({
        ...gs,
        passes: gs.passes.filter(p => filterStatus === 'ALL' || p.status === filterStatus),
      }));
  }, [data, filterStatus, filterStation]);

  const totalPasses = useMemo(
    () => filteredRows.reduce((sum, r) => sum + r.passes.length, 0),
    [filteredRows]
  );

  // ── Loading / error states ──
  if (ganttQuery.isLoading) {
    return (
      <div className="loading-overlay">
        <div className="spinner spinner-lg" />
        <span>載入甘特圖資料中…</span>
      </div>
    );
  }

  if (ganttQuery.isError || !data) {
    return (
      <div className="alert alert-danger">
        無法載入甘特圖資料。請確認 Session 已執行排程後再試。
      </div>
    );
  }

  const canEdit = true; // could be tied to session status

  return (
    <div>
      {/* Toolbar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 12,
          marginBottom: 16,
          flexWrap: 'wrap',
        }}
      >
        {/* Zoom */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 12, color: '#5f6368' }}>縮放</span>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => setPxPerMin(p => Math.max(p - 0.5, MIN_PX_PER_MIN))}
            title="縮小"
          >
            −
          </button>
          <span style={{ fontSize: 12, minWidth: 36, textAlign: 'center' }}>
            {pxPerMin.toFixed(1)}×
          </span>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => setPxPerMin(p => Math.min(p + 0.5, MAX_PX_PER_MIN))}
            title="放大"
          >
            +
          </button>
        </div>

        <div className="divider" style={{ margin: 0, width: 1, height: 24, borderTop: 'none', borderLeft: '1px solid #dadce0' }} />

        {/* Filter by status */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 12, color: '#5f6368' }}>狀態</span>
          <select
            className="form-control"
            style={{ padding: '4px 8px', fontSize: 12, height: 30 }}
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
          >
            <option value="ALL">全部</option>
            {Object.entries(PASS_LABEL).map(([v, l]) => (
              <option key={v} value={v}>{l}</option>
            ))}
          </select>
        </div>

        {/* Filter by station */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 12, color: '#5f6368' }}>地面站</span>
          <select
            className="form-control"
            style={{ padding: '4px 8px', fontSize: 12, height: 30 }}
            value={filterStation}
            onChange={e => setFilterStation(Number(e.target.value))}
          >
            <option value={0}>全部</option>
            {data.groundStations.map(gs => (
              <option key={gs.groundStationId} value={gs.groundStationId}>
                {gs.groundStationName}
              </option>
            ))}
          </select>
        </div>

        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 12, color: '#5f6368' }}>
            顯示 {totalPasses} 個 Pass
          </span>
          {canEdit && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => setShowAddForm(true)}
            >
              + 手動新增 Pass
            </button>
          )}
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => qc.invalidateQueries({ queryKey: ['gantt', sessionId] })}
          >
            ↻ 重新整理
          </button>
        </div>
      </div>

      {/* Legend */}
      <div style={{ marginBottom: 12 }}>
        <Legend />
      </div>

      {/* Chart */}
      <div
        className="card"
        style={{ overflowX: 'auto', overflowY: 'auto', maxHeight: '65vh' }}
      >
        <TimeRuler epochStart={epochStart} totalMin={totalMin} pxPerMin={pxPerMin} />

        {filteredRows.length === 0 ? (
          <div className="empty-state" style={{ minHeight: 200 }}>
            <div className="empty-state-icon">📡</div>
            <div className="empty-state-title">無資料</div>
            <div className="empty-state-description">尚無 Pass 符合篩選條件</div>
          </div>
        ) : (
          filteredRows.map(row => (
            <GanttRow
              key={row.groundStationId}
              row={row}
              epochStart={epochStart}
              totalMin={totalMin}
              pxPerMin={pxPerMin}
              onDeletePass={handleDeletePass}
              onDropPass={handleDropPass}
              draggedPass={draggedPass}
              canEdit={canEdit}
            />
          ))
        )}
      </div>

      {/* Sidebar Toggle Handle (when closed) */}
      {!isSidebarOpen && (
        <button
          onClick={() => setIsSidebarOpen(true)}
          style={{
            position: 'fixed',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            width: 24,
            height: 60,
            background: '#1a73e8',
            color: '#fff',
            border: 'none',
            borderRadius: '8px 0 0 8px',
            cursor: 'pointer',
            zIndex: 101,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '-2px 0 10px rgba(0,0,0,0.2)',
          }}
          title="顯示待排程面板"
        >
          ◀
        </button>
      )}

      {/* Sidebar Toggle button in Toolbar */}
      <div style={{ marginTop: 20, display: 'flex', justifyContent: 'flex-end' }}>
        <button
          className={`btn ${isSidebarOpen ? 'btn-secondary' : 'btn-primary'} btn-sm`}
          onClick={() => setIsSidebarOpen(!isSidebarOpen)}
        >
          {isSidebarOpen ? '隱藏待排程面板' : '顯示待排程面板'}
        </button>
      </div>

      <div style={{
        position: 'fixed',
        right: isSidebarOpen ? 0 : -260,
        top: 64, // below header
        bottom: 0,
        width: 260,
        zIndex: 100,
        transition: 'right 0.3s ease',
        boxShadow: '-2px 0 10px rgba(0,0,0,0.1)',
      }}>
        {/* Sidebar Close Tab */}
        {isSidebarOpen && (
          <div
            onClick={() => setIsSidebarOpen(false)}
            style={{
              position: 'absolute',
              left: -20,
              top: '50%',
              transform: 'translateY(-50%)',
              width: 20,
              height: 48,
              background: '#fff',
              border: '1px solid #dadce0',
              borderRight: 'none',
              borderRadius: '6px 0 0 6px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 10,
              color: '#5f6368',
            }}
          >
            ▶
          </div>
        )}
        <UnscheduledPassPanel
          data={data}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
        />
      </div>

      {/* Manual Pass Modal */}
      {showAddForm && (
        <div className="modal-backdrop" onClick={() => setShowAddForm(false)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">手動新增 Pass</h3>
              <button className="modal-close" onClick={() => setShowAddForm(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info" style={{ marginBottom: 16 }}>
                手動新增的 Pass 將標記為「強制（FORCED）」狀態，不受衝突縮短策略影響。
              </div>
              <ManualPassForm
                sessionId={sessionId}
                data={data}
                onSuccess={() => setShowAddForm(false)}
                onCancel={() => setShowAddForm(false)}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
