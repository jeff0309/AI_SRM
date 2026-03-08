import type { 
  ApiResponse, Page, GroundStation, Satellite, ScheduleSession, GanttChartData, 
  LoginResponse, StationUnavailability, PassItem, GroundStationRow
} from '../types';

const now = new Date();
const isoNow = now.toISOString();

const getTime = (offsetHours: number) => {
  const d = new Date(now);
  d.setHours(d.getHours() + offsetHours);
  return d.toISOString();
};

export const mockLoginResponse: LoginResponse = {
  token: 'mock-jwt-token',
  refreshToken: 'mock-refresh-token',
  expiresIn: 3600,
  userId: 1,
  username: 'admin',
  role: 'ADMIN',
};

export const mockGroundStations: GroundStation[] = [
  { id: 1, name: '台北地面站 (TPE-01)', code: 'TPE-01', longitude: 121.5, latitude: 25.0, setupTime: 5, teardownTime: 5, frequencyBand: 'X', enabled: true },
  { id: 2, name: '高雄地面站 (KHH-01)', code: 'KHH-01', longitude: 120.3, latitude: 22.6, setupTime: 10, teardownTime: 10, frequencyBand: 'S', enabled: true },
  { id: 3, name: '東京地面站 (TYO-03)', code: 'TYO-03', longitude: 139.7, latitude: 35.6, setupTime: 8, teardownTime: 8, frequencyBand: 'X', enabled: true },
  { id: 4, name: '華盛頓地面站 (IAD-05)', code: 'IAD-05', longitude: -77.0, latitude: 38.9, setupTime: 15, teardownTime: 15, frequencyBand: 'XS', enabled: true },
  { id: 5, name: '倫敦地面站 (LHR-02)', code: 'LHR-02', longitude: -0.1, latitude: 51.5, setupTime: 5, teardownTime: 5, frequencyBand: 'S', enabled: false },
];

export const mockSatellites: Satellite[] = [
  { id: 1, name: '福衛五號 (FS-5)', code: 'FS5', company: 'NSPO', frequencyBand: 'X', minDailyPasses: 2, minPassDuration: 300, priorityWeight: 10, isEmergency: false, enabled: true },
  { id: 2, name: '福衛七號 (FS-7)', code: 'FS7', company: 'NSPO', frequencyBand: 'S', minDailyPasses: 4, minPassDuration: 200, priorityWeight: 5, isEmergency: false, enabled: true },
  { id: 3, name: '玉山衛星 (YUSAT)', code: 'YUSAT', company: 'MoST', frequencyBand: 'S', minDailyPasses: 1, minPassDuration: 120, priorityWeight: 2, isEmergency: false, enabled: true },
  { id: 4, name: '飛鼠衛星 (IDEASSAT)', code: 'IDEAS', company: 'NCU', frequencyBand: 'X', minDailyPasses: 1, minPassDuration: 180, priorityWeight: 3, isEmergency: true, enabled: true },
  { id: 5, name: '阿里山一號', code: 'ALS1', company: 'TASA', frequencyBand: 'XS', minDailyPasses: 2, minPassDuration: 400, priorityWeight: 8, isEmergency: false, enabled: true },
];

export const mockCompanies = ['NSPO', 'MoST', 'NCU', 'TASA', 'SpaceX', 'ESA'];
export const mockStrategies = ['Proportional', 'PriorityFirst', 'MaxThroughput', 'NoShortening'];

export const mockSessions: Page<ScheduleSession> = {
  content: [
    {
      id: 1, name: '2026-03-10 定期排程', description: '每週二例行性資源排程任務',
      scheduleStartTime: getTime(0), scheduleEndTime: getTime(24), status: 'COMPLETED',
      shorteningStrategy: 'Proportional', totalRequests: 45, scheduledCount: 38, rejectedCount: 7,
      createdAt: getTime(-48), executedAt: getTime(-2),
    },
    {
      id: 2, name: '緊急任務排程 (福衛五號)', description: '針對 FS-5 的緊急軌道調整排程',
      scheduleStartTime: getTime(1), scheduleEndTime: getTime(5), status: 'DRAFT',
      shorteningStrategy: 'PriorityFirst', totalRequests: 8, scheduledCount: 0, rejectedCount: 0,
      createdAt: getTime(-1),
    },
    {
      id: 3, name: '跨國觀測協作計畫', description: '與 ESA 合作之地面站資源分配',
      scheduleStartTime: getTime(48), scheduleEndTime: getTime(96), status: 'PROCESSING',
      shorteningStrategy: 'MaxThroughput', totalRequests: 120, scheduledCount: 85, rejectedCount: 12,
      createdAt: getTime(-12),
    },
  ],
  totalElements: 3, totalPages: 1, size: 20, number: 0,
};

const createPass = (id: number, satId: number, satName: string, band: any, start: number, end: number): PassItem => ({
  passId: id, satelliteId: satId, satelliteName: satName, frequencyBand: band,
  originalAos: getTime(start), originalLos: getTime(end),
  scheduledAos: getTime(start), scheduledLos: getTime(end),
  status: 'SCHEDULED', isAllowed: true, isForced: false, durationSeconds: (end - start) * 3600,
});

export const mockGanttData: GanttChartData = {
  sessionId: 1, sessionName: '2026-03-10 定期排程',
  scheduleStartTime: getTime(0), scheduleEndTime: getTime(24),
  groundStations: [
    {
      groundStationId: 1, groundStationName: '台北地面站 (TPE-01)', frequencyBand: 'X',
      unavailabilities: [],
      passes: [
        createPass(101, 1, '福衛五號 (FS-5)', 'X', 1, 1.5),
        createPass(102, 4, '飛鼠衛星 (IDEASSAT)', 'X', 4, 4.8),
        createPass(103, 1, '福衛五號 (FS-5)', 'X', 9, 10),
      ],
    },
    {
      groundStationId: 2, groundStationName: '高雄地面站 (KHH-01)', frequencyBand: 'S',
      unavailabilities: [{ id: 1, startTime: getTime(2), endTime: getTime(4), reason: '設備例行維修' }],
      passes: [
        createPass(201, 2, '福衛七號 (FS-7)', 'S', 0.5, 1),
        createPass(202, 3, '玉山衛星 (YUSAT)', 'S', 6, 7),
        createPass(203, 2, '福衛七號 (FS-7)', 'S', 12, 13.5),
      ],
    },
    {
      groundStationId: 3, groundStationName: '東京地面站 (TYO-03)', frequencyBand: 'X',
      unavailabilities: [],
      passes: [
        createPass(301, 1, '福衛五號 (FS-5)', 'X', 2, 3),
        createPass(302, 4, '飛鼠衛星 (IDEASSAT)', 'X', 7, 7.5),
      ],
    },
  ],
};

export const mockUnavailabilities: StationUnavailability[] = [
  { id: 101, startTime: getTime(-24), endTime: getTime(-20), reason: '天線校正', maintenanceType: 'PREVENTIVE', isRecurring: false },
  { id: 102, startTime: getTime(2), endTime: getTime(6), reason: '電力系統升級', maintenanceType: 'UPGRADE', isRecurring: false },
  { id: 103, startTime: getTime(48), endTime: getTime(50), reason: '國定假日停工', maintenanceType: 'HOLIDAY', isRecurring: true },
];

export const wrapResponse = <T>(data: T): ApiResponse<T> => ({
  success: true, message: 'Success (Mock Mode)', data, timestamp: new Date().toISOString(),
});
