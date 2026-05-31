import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('auth');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        setUser(parsed);
        authService.setToken(parsed.token);
      } catch {
        localStorage.removeItem('auth');
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    const data = await authService.login(username, password);
    authService.setToken(data.token);
    setUser(data);
    localStorage.setItem('auth', JSON.stringify(data));
    return data;
  }, []);

  const logout = useCallback(() => {
    authService.clearToken();
    setUser(null);
    localStorage.removeItem('auth');
  }, []);

  const hasRole = useCallback(
    (roles) => {
      if (!user) return false;
      if (!roles || roles.length === 0) return true;
      return roles.some((r) => user.roles?.includes(r));
    },
    [user]
  );

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
};
