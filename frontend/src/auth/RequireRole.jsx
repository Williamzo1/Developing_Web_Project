import React from 'react';
import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { roleHomes, useAuth } from './AuthContext';

export default function RequireRole({ role }) {
  const { actor, error, loading, retry, signedOut } = useAuth();
  const location = useLocation();
  if (loading) return <p role="status" className="notice">Đang kiểm tra phiên đăng nhập…</p>;
  if (error) return <div className="notice"><p role="alert">{error.message}</p>
    <button className="button" type="button" onClick={retry}>Thử lại</button></div>;
  if (!actor && signedOut) return <Navigate to={signedOut.to} state={signedOut.state} replace />;
  if (!actor) return <Navigate to="/dang-nhap" state={{ from: location.pathname + location.search }} replace />;
  if (actor.role !== role) return <div className="notice">
    <h1>Không có quyền truy cập</h1><Link to={roleHomes[actor.role] || '/'}>Về khu vực của bạn</Link>
  </div>;
  return <Outlet />;
}
