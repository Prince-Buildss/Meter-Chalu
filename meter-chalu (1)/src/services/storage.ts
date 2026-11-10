import { HomeReport } from '../types';

const STORAGE_KEYS = {
  BACKEND_URL: 'meterchalu_backend_url',
  MOCK_MODE: 'meterchalu_mock_mode',
  ROOM_NAME: 'meterchalu_room_name',
  DRIVER_NAME: 'meterchalu_driver_name',
  HELPER_LANG: 'meterchalu_helper_lang',
  ONBOARDING_DONE: 'meterchalu_onboarding_done',
  CACHED_REPORT: 'meterchalu_cached_report',
  OVERALL_READINESS: 'meterchalu_overall_readiness',
};

export function sanitizeBackendUrl(url: string): string {
  let clean = url.trim();
  // Remove trailing slashes
  while (clean.endsWith('/')) {
    clean = clean.slice(0, -1);
  }
  // Strip trailing /token if user entered token endpoint
  if (clean.endsWith('/token')) {
    clean = clean.slice(0, -6).trim();
    while (clean.endsWith('/')) {
      clean = clean.slice(0, -1);
    }
  }
  return clean;
}

export function getBackendUrl(): string {
  // Read fresh every time from localStorage, never cache in module variable
  const stored = localStorage.getItem(STORAGE_KEYS.BACKEND_URL);
  if (stored) {
    return sanitizeBackendUrl(stored);
  }
  // Default fallback from Vite env if present
  const envUrl = (import.meta as any).env?.VITE_API_BASE || '';
  return sanitizeBackendUrl(envUrl);
}

export function setBackendUrl(url: string): void {
  const sanitized = sanitizeBackendUrl(url);
  localStorage.setItem(STORAGE_KEYS.BACKEND_URL, sanitized);
}

export function getIsMockMode(): boolean {
  // Mock mode must be a runtime setting, default is OFF (false)
  const stored = localStorage.getItem(STORAGE_KEYS.MOCK_MODE);
  if (stored !== null) {
    return stored === 'true';
  }
  return false;
}

export function setIsMockMode(val: boolean): void {
  localStorage.setItem(STORAGE_KEYS.MOCK_MODE, val ? 'true' : 'false');
}

export function getStableRoomName(): string {
  let room = localStorage.getItem(STORAGE_KEYS.ROOM_NAME);
  if (!room || !room.startsWith('driver-') || room.length < 10) {
    // Generate strictly ASCII 8-character lowercase hex id
    const hex = Math.floor(Math.random() * 0xffffffff).toString(16).padStart(8, '0');
    room = `driver-${hex}`;
    localStorage.setItem(STORAGE_KEYS.ROOM_NAME, room);
  }
  return room;
}

export function getDriverName(): string {
  return localStorage.getItem(STORAGE_KEYS.DRIVER_NAME) || 'रमेश भाई';
}

export function setDriverName(name: string): void {
  localStorage.setItem(STORAGE_KEYS.DRIVER_NAME, name.trim());
}

export function getHelperLanguage(): string {
  const stored = localStorage.getItem(STORAGE_KEYS.HELPER_LANG);
  if (!stored) {
    localStorage.setItem(STORAGE_KEYS.HELPER_LANG, 'hi');
    return 'hi';
  }
  return stored;
}

export function setHelperLanguage(lang: string): void {
  const code = lang && lang.trim() ? lang.trim() : 'hi';
  localStorage.setItem(STORAGE_KEYS.HELPER_LANG, code);
}

export function isOnboardingCompleted(): boolean {
  return localStorage.getItem(STORAGE_KEYS.ONBOARDING_DONE) === 'true';
}

export function setOnboardingCompleted(): void {
  localStorage.setItem(STORAGE_KEYS.ONBOARDING_DONE, 'true');
}

export function getCachedReport(): HomeReport | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.CACHED_REPORT);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch (e) {
    console.error('Failed to parse cached report', e);
  }
  return null;
}

export function setCachedReport(report: HomeReport): void {
  try {
    localStorage.setItem(STORAGE_KEYS.CACHED_REPORT, JSON.stringify(report));
    if (typeof report.percent === 'number') {
      localStorage.setItem(STORAGE_KEYS.OVERALL_READINESS, report.percent.toString());
    }
  } catch (e) {
    console.error('Failed to save cached report', e);
  }
}

export function getOverallReadiness(): number {
  const cached = getCachedReport();
  if (cached && typeof cached.percent === 'number') {
    return cached.percent;
  }
  const raw = localStorage.getItem(STORAGE_KEYS.OVERALL_READINESS);
  return raw ? parseInt(raw, 10) : 45;
}

export function clearDriverData(): void {
  localStorage.removeItem(STORAGE_KEYS.CACHED_REPORT);
  localStorage.removeItem(STORAGE_KEYS.OVERALL_READINESS);
}
