import type { 
  ApiResponse, Page, GroundStation, Satellite, ScheduleSession, GanttChartData, LoginResponse 
} from '../types';

const now = new Date().toISOString();
const tomorrow = new Date(Date.now() + 86400000).toISOString();

export const mockLoginResponse: LoginResponse = {
  token: 'mock-jwt-token',
  refreshToken: 'mock-refresh-token',
  expiresIn: 3600,
  userId: 1,
  username: 'admin',
  role: 'ADMIN',
};

export const mockGroundStations: GroundStation[] = [
  {
    id: 1, name: '台北地面站', code: 'TPE-01', longitude: 121.5, latitude: 25.0, 
    setupTime: 5, teardownTime: 5, frequencyBand: 'X', enabled: true,
  },
  {
    id: 2, name: '高雄地面站', code: 'KHH-01', longitude: 120.3, latitude: 22.6, 
    setupTime: 10, teardownTime: 10, frequencyBand: 'S', enabled: true,
  },
];

export const mockSatellites: Satellite[] = [
  {
    id: 1, name: '福衛五號', code: 'FS5', company: 'NSPO', frequencyBand: 'X', 
    minDailyPasses: 2, minPassDuration: 300, priorityWeight: 10, isEmergency: false, enabled: true,
  },
  {
    id: 2, name: '福衛七號', code: 'FS7', company: 'NSPO', frequencyBand: 'S', 
    minDailyPasses: 4, minPassDuration: 200, priorityWeight: 5, isEmergency: false, enabled: true,
  },
];

export const mockSessions: Page<ScheduleSession> = {
  content: [
    {
      id: 1, name: '2026-03-10 排程測試', description: '這是模擬資料的排程會話',
      scheduleStartTime: now, scheduleEndTime: tomorrow, status: 'COMPLETED',
      shorteningStrategy: 'Proportional', totalRequests: 15, scheduledCount: 12, rejectedCount: 3,
      createdAt: now, executedAt: now,
    },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 20,
  number: 0,
};

export const mockGanttData: GanttChartData = {
  sessionId: 1,
  sessionName: '2026-03-10 排程測試',
  scheduleStartTime: now,
  scheduleEndTime: tomorrow,
  groundStations: [
    {
      groundStationId: 1,
      groundStationName: '台北地面站',
      frequencyBand: 'X',
      unavailabilities: [],
      passes: [
        {
          passId: 101, satelliteId: 1, satelliteName: '福衛五號', frequencyBand: 'X',
          originalAos: now, originalLos: tomorrow, scheduledAos: now, scheduledLos: tomorrow,
          status: 'SCHEDULED', isAllowed: true, isForced: false, durationSeconds: 600,
        },
      ],
    },
  ],
};

export const wrapResponse = <T>(data: T): ApiResponse<T> => ({
  success: true,
  message: 'Success (Mock Mode)',
  data,
  timestamp: new Date().toISOString(),
});
