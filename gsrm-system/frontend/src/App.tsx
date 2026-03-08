import { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/common/Layout';
import PrivateRoute from './components/common/PrivateRoute';

/* ---------- Lazy-loaded page components ---------- */
const LoginPage            = lazy(() => import('./pages/LoginPage'));
const ScheduleSessionsPage = lazy(() => import('./pages/ScheduleSessionsPage'));
const GroundStationsPage   = lazy(() => import('./pages/GroundStationsPage'));
const SatellitesPage       = lazy(() => import('./pages/SatellitesPage'));
const ImportPage           = lazy(() => import('./pages/ImportPage'));
const HistoryPage          = lazy(() => import('./pages/HistoryPage'));

/** Full-page loading fallback shown while a chunk is being fetched. */
function PageSpinner() {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        flexDirection: 'column',
        gap: 16,
        color: '#5f6368',
      }}
    >
      <div className="spinner spinner-lg" />
      <span style={{ fontSize: 14 }}>載入中…</span>
    </div>
  );
}

/**
 * Root application component.
 * Defines the top-level routing structure:
 *  - /login          → public
 *  - /*              → protected (wrapped in Layout + PrivateRoute)
 */
export default function App() {
  return (
    <Suspense fallback={<PageSpinner />}>
      <Routes>
        {/* Public route */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected routes — all rendered inside the main Layout */}
        <Route
          path="/*"
          element={
            <PrivateRoute>
              <Layout>
                <Suspense fallback={<PageSpinner />}>
                  <Routes>
                    <Route index element={<ScheduleSessionsPage />} />
                    <Route path="ground-stations" element={<GroundStationsPage />} />
                    <Route path="satellites"      element={<SatellitesPage />} />
                    <Route path="import"          element={<ImportPage />} />
                    <Route path="history"         element={<HistoryPage />} />
                    {/* Catch-all: redirect unknown sub-paths to home */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </Suspense>
              </Layout>
            </PrivateRoute>
          }
        />

        {/* Fallback for unknown root-level paths */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
