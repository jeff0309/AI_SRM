import React, { type ReactNode } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface LayoutProps {
  children: ReactNode;
}

const NAV_ITEMS = [
  { path: '/',               label: '排程 Session' },
  { path: '/ground-stations', label: '地面站管理' },
  { path: '/satellites',      label: '衛星管理' },
  { path: '/import',          label: '資料匯入' },
  { path: '/history',         label: '歷史查詢' },
];

/**
 * 主佈局元件（側邊欄 + 頂部導覽列）.
 */
export default function Layout({ children }: LayoutProps) {
  const { user, logout } = useAuth();
  const navigate         = useNavigate();
  const location         = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={styles.wrapper}>
      {/* Sidebar */}
      <nav style={styles.sidebar}>
        <div style={styles.logo}>
          <span style={styles.logoText}>GSRM</span>
          <span style={styles.logoSub}>地面站資源管理</span>
        </div>

        <ul style={styles.navList}>
          {NAV_ITEMS.map((item) => (
            <li key={item.path}>
              <Link
                to={item.path}
                style={{
                  ...styles.navLink,
                  ...(location.pathname === item.path ? styles.navLinkActive : {}),
                }}
              >
                {item.label}
              </Link>
            </li>
          ))}
        </ul>

        <div style={styles.userInfo}>
          <div style={styles.username}>{user?.username}</div>
          <div style={styles.role}>{user?.role}</div>
          <button onClick={handleLogout} style={styles.logoutBtn}>登出</button>
        </div>
      </nav>

      {/* Main content */}
      <main style={styles.main}>
        {children}
      </main>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  wrapper: {
    display: 'flex',
    minHeight: '100vh',
    backgroundColor: '#0d1117',
    color: '#c9d1d9',
    fontFamily: "'Segoe UI', system-ui, sans-serif",
  },
  sidebar: {
    width: '220px',
    backgroundColor: '#161b22',
    borderRight: '1px solid #30363d',
    display: 'flex',
    flexDirection: 'column',
    padding: '20px 0',
    flexShrink: 0,
  },
  logo: {
    padding: '0 20px 24px',
    borderBottom: '1px solid #30363d',
    marginBottom: '16px',
  },
  logoText: {
    display: 'block',
    fontSize: '22px',
    fontWeight: 700,
    color: '#58a6ff',
    letterSpacing: '2px',
  },
  logoSub: {
    fontSize: '11px',
    color: '#8b949e',
  },
  navList: {
    listStyle: 'none',
    padding: 0,
    margin: 0,
    flex: 1,
  },
  navLink: {
    display: 'block',
    padding: '10px 20px',
    color: '#8b949e',
    textDecoration: 'none',
    fontSize: '14px',
    transition: 'background 0.15s, color 0.15s',
  },
  navLinkActive: {
    color: '#58a6ff',
    backgroundColor: '#1c2a3a',
    borderLeft: '3px solid #58a6ff',
  },
  userInfo: {
    padding: '16px 20px',
    borderTop: '1px solid #30363d',
  },
  username: {
    color: '#c9d1d9',
    fontSize: '13px',
    fontWeight: 600,
  },
  role: {
    color: '#8b949e',
    fontSize: '11px',
    marginTop: '2px',
    marginBottom: '10px',
  },
  logoutBtn: {
    backgroundColor: 'transparent',
    border: '1px solid #30363d',
    borderRadius: '6px',
    color: '#8b949e',
    cursor: 'pointer',
    fontSize: '12px',
    padding: '5px 12px',
    width: '100%',
  },
  main: {
    flex: 1,
    padding: '28px 32px',
    overflowY: 'auto',
  },
};
