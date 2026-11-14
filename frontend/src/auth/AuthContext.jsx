import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../api/hooks';

const AuthContext = createContext(null);
const storageKey = 'vcraft.session';
export const roleHomes = { BUYER: '/mua-si', ARTISAN: '/xuong', ADMIN: '/quan-tri' };
export const roleNames = { BUYER: 'Mua sỉ', ARTISAN: 'Xưởng', ADMIN: 'Quản trị' };

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => sessionStorage.getItem(storageKey));
  const [signedOut, setSignedOut] = useState(null);
  const location = useLocation();
  const { current, retry } = useApi(token ? '/api/me' : null, token);

  function save(nextToken) {
    if (nextToken) sessionStorage.setItem(storageKey, nextToken);
    else sessionStorage.removeItem(storageKey);
    setToken(nextToken);
  }

  useEffect(() => {
    if (current?.error?.status === 401) save(null);
  }, [current]);

  useEffect(() => {
    if (signedOut?.to === location.pathname) setSignedOut(null);
  }, [signedOut, location.pathname]);

  async function login(credentials) {
    const { token: nextToken, role } = await api('/api/login', { method: 'POST', body: credentials });
    setSignedOut(null);
    save(nextToken);
    return role;
  }

  async function logout() {
    try {
      await api('/api/logout', { token, method: 'POST' });
    } catch (error) {
      if (error.status !== 401) throw error;
    }
    endLocalSession('/');
  }

  function endLocalSession(to, state) {
    setSignedOut({ to, state });
    save(null);
  }

  return <AuthContext.Provider value={{
    actor: current?.data, token, error: current?.error,
    loading: Boolean(token && (!current || current.error?.status === 401)), retry, login, logout,
    endLocalSession, signedOut,
  }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
