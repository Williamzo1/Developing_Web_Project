import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { roleHomes, roleNames, useAuth } from './AuthContext';

export default function AccountNav() {
  const { actor, logout, loading } = useAuth();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  async function endSession() {
    setPending(true);
    setError(null);
    try {
      await logout();
      navigate('/', { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  if (loading) return <span role="status">Đang kiểm tra phiên…</span>;
  if (!actor) return <Link to="/dang-nhap">Đăng nhập</Link>;
  return <>
    <Link to={roleHomes[actor.role]}>{roleNames[actor.role]}</Link>
    <Link to={roleHomes[actor.role] + '/mat-khau'}>Đổi mật khẩu</Link>
    <button className="button" type="button" disabled={pending} onClick={endSession}>
      {pending ? 'Đang đăng xuất…' : 'Đăng xuất'}
    </button>
    {error && <span role="alert">{error.message}</span>}
  </>;
}
