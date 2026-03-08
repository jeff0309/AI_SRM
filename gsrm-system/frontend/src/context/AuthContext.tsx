import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { LoginResponse, UserRole } from '../types';
import { authApi } from '../api/authApi';

interface AuthUser {
  userId: number;
  username: string;
  role: UserRole;
  token: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: UserRole | UserRole[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadUserFromStorage(): AuthUser | null {
  try {
    const raw = localStorage.getItem('gsrm_user');
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

/**
 * 認證 Context Provider.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUserFromStorage);

  const login = useCallback(async (username: string, password: string) => {
    const res = await authApi.login({ username, password });
    const data: LoginResponse = res.data.data;

    const authUser: AuthUser = {
      userId: data.userId,
      username: data.username,
      role: data.role,
      token: data.token,
    };

    localStorage.setItem('gsrm_token', data.token);
    localStorage.setItem('gsrm_user', JSON.stringify(authUser));
    setUser(authUser);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('gsrm_token');
    localStorage.removeItem('gsrm_user');
    setUser(null);
    authApi.logout().catch(() => {/* ignore */});
  }, []);

  const hasRole = useCallback((role: UserRole | UserRole[]) => {
    if (!user) return false;
    const roles = Array.isArray(role) ? role : [role];
    return roles.includes(user.role);
  }, [user]);

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated: !!user,
      login,
      logout,
      hasRole,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * 取得認證 Context.
 */
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
