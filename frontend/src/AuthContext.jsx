import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import api, { prepareCsrf } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [checkingSession, setCheckingSession] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const response = await api.get('/auth/me');
      setUser(response.data.user);
    } catch {
      setUser(null);
    } finally {
      setCheckingSession(false);
    }
  }, []);

  useEffect(() => { refresh(); }, [refresh]);

  const login = useCallback(async (email, password) => {
    await prepareCsrf();
    const response = await api.post('/auth/login', { email, password });
    setUser(response.data.user);
    return response.data.user;
  }, []);

  const registerAccount = useCallback(async (details, requester = false) => {
    await prepareCsrf();
    await api.post(requester ? '/auth/register/requester' : '/auth/register', details);
    return login(details.email, details.password);
  }, [login]);

  const updateCurrentUser = useCallback((details) => {
    setUser(current => current ? { ...current, ...details } : current);
  }, []);

  const logout = useCallback(async () => {
    await prepareCsrf();
    await api.post('/auth/logout');
    setUser(null);
  }, []);

  const value = useMemo(() => ({ user, checkingSession, login, logout, registerAccount, updateCurrentUser }),
    [user, checkingSession, login, logout, registerAccount, updateCurrentUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
