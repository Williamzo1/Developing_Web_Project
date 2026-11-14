import React from 'react';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import styles from './components/Form.module.css';

export default function AdminWorkshops() {
  const path = '/api/admin/workshops';
  const { token } = useAuth();
  const { current, retry } = useApi(path, token);
  const create = useMutation(token, retry);
  const moderation = useMutation(token, retry);

  async function provision(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const body = Object.fromEntries(new FormData(form));
    if (await create.submit(path, 'POST', body)) form.reset();
  }

  return <section>
    <h1>Quản lý hộ gia đình</h1>
    <h2>Cấp tài khoản xưởng</h2>
    <form className={styles.form} onSubmit={provision}>
      <label>Tên xưởng<input name="workshopName" required maxLength={160} />
        <small>{create.error?.fieldErrors?.workshopName}</small></label>
      <label>Tên đăng nhập<input name="username" required minLength={3} maxLength={60}
        autoComplete="off" pattern="[a-z0-9-]+" />
        <small>{create.error?.fieldErrors?.username}</small></label>
      <label>Mật khẩu ban đầu<input name="password" type="password" required autoComplete="new-password" />
        <small>{create.error?.fieldErrors?.password}</small></label>
      {create.error && <p role="alert">{create.error.message}</p>}
      <button className="button" disabled={create.pending}>Cấp tài khoản</button>
    </form>
    <h2>Danh sách xưởng</h2>
    {moderation.error && <p role="alert">{moderation.error.message}</p>}
    {!current?.data ? <DataState current={current} retry={retry} /> :
      current.data.length === 0 ? <p className="notice">Chưa có xưởng.</p> :
      <ul className="grid">{current.data.map(workshop => <li className="card card-copy" key={workshop.id}>
        <h2>{workshop.name}</h2>
        {workshop.username && <p>{workshop.username}</p>}
        <p>{workshop.approved ? 'Đã duyệt' : 'Chưa duyệt hoặc đã khóa'}</p>
        <button type="button" className="button" disabled={moderation.pending}
          onClick={() => moderation.submit(path + '/' + workshop.id + (workshop.approved ? '/suspend' : '/approve'), 'POST')}>
          {workshop.approved ? 'Khóa xưởng' : 'Duyệt xưởng'}
        </button>
      </li>)}</ul>}
  </section>;
}
