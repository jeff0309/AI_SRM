import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * 登入頁面.
 */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate   = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch {
      setError('使用者名稱或密碼錯誤');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>GSRM 地面站資源管理系統</h1>
        <h2 style={styles.subtitle}>使用者登入</h2>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.field}>
            <label style={styles.label}>使用者名稱</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              style={styles.input}
              placeholder="請輸入使用者名稱"
              required
              autoFocus
            />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>密碼</label>
            <div style={styles.passwordWrapper}>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ ...styles.input, paddingRight: '40px', width: '100%' }}
                placeholder="請輸入密碼"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={styles.toggleBtn}
                title={showPassword ? '隱藏密碼' : '顯示密碼'}
              >
                {showPassword ? '👁️' : '🕶️'}
              </button>
            </div>
          </div>

          <button type="submit" style={styles.button} disabled={loading}>
            {loading ? '登入中...' : '登入'}
          </button>
        </form>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#1a1a2e',
    padding: '20px',
  },
  card: {
    backgroundColor: '#16213e',
    borderRadius: '12px',
    padding: '48px 40px',
    width: '100%',
    maxWidth: '420px',
    boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
    border: '1px solid #0f3460',
  },
  title: {
    color: '#e94560',
    fontSize: '20px',
    fontWeight: 700,
    textAlign: 'center',
    marginBottom: '8px',
  },
  subtitle: {
    color: '#a8b2d8',
    fontSize: '14px',
    fontWeight: 400,
    textAlign: 'center',
    marginBottom: '32px',
  },
  error: {
    backgroundColor: '#3d1515',
    border: '1px solid #e94560',
    color: '#ff6b6b',
    borderRadius: '6px',
    padding: '10px 14px',
    marginBottom: '20px',
    fontSize: '14px',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    color: '#8892b0',
    fontSize: '13px',
    fontWeight: 500,
  },
  passwordWrapper: {
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
  },
  input: {
    backgroundColor: '#0f3460',
    border: '1px solid #1a4a7a',
    borderRadius: '6px',
    color: '#ccd6f6',
    fontSize: '14px',
    padding: '10px 14px',
    outline: 'none',
  },
  toggleBtn: {
    position: 'absolute',
    right: '8px',
    background: 'none',
    border: 'none',
    color: '#8892b0',
    cursor: 'pointer',
    fontSize: '16px',
    padding: '4px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    backgroundColor: '#e94560',
    border: 'none',
    borderRadius: '6px',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '15px',
    fontWeight: 600,
    padding: '12px',
    marginTop: '8px',
  },
};
