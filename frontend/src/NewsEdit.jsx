import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from './api/client';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import { newsKinds } from './NewsList';
import styles from './components/Form.module.css';

const apiPath = '/api/admin/news';
const route = '/quan-tri/tin-tuc';

function localTime(value) {
  if (!value) return '';
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
}

export default function NewsEdit() {
  const { id } = useParams();
  const { token } = useAuth();
  const result = useApi(id ? apiPath + '/' + encodeURIComponent(id) : null, token);
  if (id && !result.current?.data) return <DataState {...result} missing="Không tìm thấy bài viết." />;
  return <NewsForm key={id || 'new'} news={result.current?.data} token={token} />;
}

function NewsForm({ news, token }) {
  const [kind, setKind] = useState(news?.kind || 'POLICY');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  async function save(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = Object.fromEntries(form);
    body.hidden = form.has('hidden');
    for (const key of ['eventStart', 'eventEnd']) body[key] = body[key] ? new Date(body[key]).toISOString() : null;
    setPending(true);
    setError(null);
    try {
      await api(apiPath + (news ? '/' + news.id : ''), { token, method: news ? 'PUT' : 'POST', body });
      navigate(route, { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return <section>
    <Link className="back" to={route}>← Về danh sách tin</Link>
    <h1>{news ? 'Sửa bài viết' : 'Đăng tin'}</h1>
    <form className={styles.form} onSubmit={save}>
      <label>Tiêu đề<input name="title" maxLength={160} required defaultValue={news?.title} /></label>
      <label>Loại tin<select name="kind" value={kind} onChange={event => setKind(event.target.value)}>
        {Object.entries(newsKinds).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
      </select></label>
      <label>Nội dung<textarea name="body" rows={10} maxLength={50000} required defaultValue={news?.body} /></label>
      {kind === 'EVENT' && <>
        <label>Bắt đầu<input type="datetime-local" name="eventStart" required defaultValue={localTime(news?.eventStart)} /></label>
        <label>Kết thúc<input type="datetime-local" name="eventEnd" defaultValue={localTime(news?.eventEnd)} /></label>
        <label>Địa điểm<input name="eventPlace" maxLength={300} required defaultValue={news?.eventPlace} /></label>
      </>}
      <label className={styles.checkbox}><input name="hidden" type="checkbox" defaultChecked={news?.hidden} />Ẩn bài viết</label>
      {error && <p role="alert">{error.message} {Object.values(error.fieldErrors || {}).join(' ')}</p>}
      <button className="button" disabled={pending}>Lưu bài viết</button>
    </form>
  </section>;
}
