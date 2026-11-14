import React from 'react';
import { useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import styles from './components/Form.module.css';

export default function ChangePassword() {
  const { token, endLocalSession } = useAuth();
  const mutation = useMutation(token);
  return <section className={styles.panel}>
    <h1>Đổi mật khẩu</h1>
    <form className={styles.form} onSubmit={async event => {
      event.preventDefault();
      const body = Object.fromEntries(new FormData(event.currentTarget));
      if (await mutation.submit('/api/password', 'PUT', body)) {
        endLocalSession('/dang-nhap', { passwordChanged: true });
      }
    }}>
      <label>Mật khẩu hiện tại<input type="password" autoComplete="current-password" name="currentPassword" required /></label>
      <label>Mật khẩu mới<input type="password" autoComplete="new-password" name="newPassword" required minLength={10} /></label>
      {mutation.error && <p role="alert">{mutation.error.message} {Object.values(mutation.error.fieldErrors || {}).join(' ')}</p>}
      <button className="button" disabled={mutation.pending}>Đổi mật khẩu</button>
    </form>
  </section>;
}
