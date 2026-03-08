// ─────────── Enums ───────────

export type FrequencyBand = 'X' | 'S' | 'XS';
export type PassStatus = 'PENDING' | 'SCHEDULED' | 'SHORTENED' | 'REJECTED' | 'FORCED';
export type ScheduleStatus = 'DRAFT' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
export type UserRole = 'ADMIN' | 'OPERATOR' | 'VIEWER';

// ─────────── Common ───────────

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ─────────── Auth ───────────

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  userId: number;
  username: string;
  role: UserRole;
}

// ─────────── User ───────────

export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string;
  role: UserRole;
  enabled: boolean;
  locked: boolean;
  lastLoginAt?: string;
  createdAt: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  email: string;
  displayName?: string;
  role: UserRole;
}

// ─────────── Ground Station ───────────

export interface GroundStation {
  id: number;
  name: string;
  code?: string;
  longitude: number;
  latitude: number;
  altitude?: number;
  setupTime: number;
  teardownTime: number;
  frequencyBand: FrequencyBand;
  minElevation?: number;
  enabled: boolean;
  description?: string;
  contactPerson?: string;
  contactPhone?: string;
}

export interface GroundStationRequest {
  name: string;
  code?: string;
  longitude: number;
  latitude: number;
  altitude?: number;
  setupTime: number;
  teardownTime: number;
  frequencyBand: FrequencyBand;
  minElevation?: number;
  description?: string;
  contactPerson?: string;
  contactPhone?: string;
  enabled?: boolean;
}

export interface StationUnavailability {
  id: number;
  groundStation?: GroundStation;
  startTime: string;
  endTime: string;
  reason?: string;
  maintenanceType?: string;
  isRecurring: boolean;
}

// ─────────── Satellite ───────────

export interface Satellite {
  id: number;
  name: string;
  code?: string;
  company?: string;
  frequencyBand: FrequencyBand;
  minDailyPasses: number;
  minPassDuration: number;
  priorityWeight: number;
  isEmergency: boolean;
  enabled: boolean;
  description?: string;
  contactPerson?: string;
  contactEmail?: string;
}

export interface GroundStationPreferenceDto {
  groundStationId: number;
  preferenceOrder: number;
  isMandatory?: boolean;
}

export interface SatelliteCreateRequest {
  name: string;
  code?: string;
  company?: string;
  frequencyBand: FrequencyBand;
  minDailyPasses: number;
  minPassDuration: number;
  priorityWeight: number;
  isEmergency?: boolean;
  description?: string;
  contactPerson?: string;
  contactEmail?: string;
  enabled?: boolean;
  groundStationPreferences?: GroundStationPreferenceDto[];
}

// ─────────── Schedule Session ───────────

export interface ScheduleSession {
  id: number;
  name: string;
  description?: string;
  scheduleStartTime: string;
  scheduleEndTime: string;
  status: ScheduleStatus;
  shorteningStrategy: string;
  totalRequests: number;
  scheduledCount: number;
  rejectedCount: number;
  executedAt?: string;
  createdAt: string;
  satelliteIds?: number[];
  groundStationIds?: number[];
}

export interface ScheduleSessionRequest {
  name: string;
  description?: string;
  scheduleStartTime: string;
  scheduleEndTime: string;
  satelliteIds?: number[];
  groundStationIds?: number[];
  shorteningStrategy?: string;
}

// ─────────── Gantt Chart ───────────

export interface PassItem {
  passId: number;
  satelliteId: number;
  satelliteName: string;
  frequencyBand: FrequencyBand;
  originalAos: string;
  originalLos: string;
  scheduledAos: string;
  scheduledLos: string;
  status: PassStatus;
  isAllowed: boolean;
  isForced: boolean;
  shortenedSeconds?: number;
  durationSeconds?: number;
  notes?: string;
}

export interface UnavailabilityItem {
  id: number;
  startTime: string;
  endTime: string;
  reason?: string;
}

export interface GroundStationRow {
  groundStationId: number;
  groundStationName: string;
  frequencyBand: FrequencyBand;
  passes: PassItem[];
  unavailabilities: UnavailabilityItem[];
}

export interface GanttChartData {
  sessionId: number;
  sessionName: string;
  scheduleStartTime: string;
  scheduleEndTime: string;
  groundStations: GroundStationRow[];
}

// ─────────── Schedule Result ───────────

export interface ScheduleResultResponse {
  sessionId: number;
  sessionName: string;
  status: ScheduleStatus;
  scheduleStartTime: string;
  scheduleEndTime: string;
  executedAt?: string;
  totalRequests: number;
  scheduledCount: number;
  shortenedCount: number;
  rejectedCount: number;
  forcedCount: number;
  successRate: number;
  conflictsResolved: number;
  strategyUsed: string;
  executionTimeMs: number;
}

// ─────────── Manual Pass ───────────

export interface ManualPassRequest {
  sessionId: number;
  satelliteId: number;
  groundStationId: number;
  frequencyBand: FrequencyBand;
  aos: string;
  los: string;
  notes?: string;
}
