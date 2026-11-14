import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { roleHomes, useAuth } from './AuthContext';
import styles from '../components/Form.module.css';

export default function Login() {
  const { actor, login, loading, error: sessionError, retry } = useAuth();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from;

  function destination(role) {
    const home = roleHomes[role];
    return home && (from === home || from?.startsWith(home + '/') || from?.startsWith(home + '?'))
      ? from : home || '/';
  }

  async function submit(event) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setPending(true);
    setError(null);
    try {
      const role = await login({ username: data.get('username').trim(), password: data.get('password') });
      navigate(destination(role), { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  if (actor) return <Navigate replace to={destination(actor.role)} />;
  if (loading) return <p className="notice" role="status">Đang kiểm tra phiên đăng nhập…</p>;
  return <section className={styles.panel}>
    <h1>Đăng nhập</h1>
    {location.state?.passwordChanged && <p role="status">Đã đổi mật khẩu. Vui lòng đăng nhập lại.</p>}
    {sessionError && <p role="alert">{sessionError.message}{' '}
      <button type="button" onClick={retry}>Thử lại</button></p>}
    <form className={styles.form} onSubmit={submit}>
      <label>Tên đăng nhập
        <input name="username" autoComplete="username" required maxLength={60} aria-describedby="username-error" />
        {error?.fieldErrors?.username && <small id="username-error">{error.fieldErrors.username}</small>}
      </label>
      <label>Mật khẩu
        <input name="password" type="password" autoComplete="current-password" required aria-describedby="password-error" />
        {error?.fieldErrors?.password && <small id="password-error">{error.fieldErrors.password}</small>}
      </label>
      {error && <p role="alert">{error.message}</p>}
      <button type="submit" className="button" disabled={pending}>{pending ? 'Đang đăng nhập…' : 'Đăng nhập'}</button>
    </form>
  </section>;
}
