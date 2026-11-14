import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from './api/client';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import { quotePath, requestPath } from './QuoteRequests';
import styles from './components/Form.module.css';

export default function QuoteDraft() {
  const { id } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const path = quotePath(true) + (id ? '/' + encodeURIComponent(id) : '');
  const result = useApi(id ? path : null, token);
  const categories = useApi('/api/categories');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(null);
  const draft = result.current?.data?.request;
  if (id && !draft) return <DataState {...result} missing="Không tìm thấy yêu cầu." />;
  if (!categories.current?.data) return <DataState {...categories} />;
  if (draft && draft.status !== 'DRAFT') return <p className="notice">Yêu cầu đã gửi.{' '}
    <Link to={requestPath(true) + '/' + id}>Xem yêu cầu</Link></p>;

  async function save(event) {
    event.preventDefault();
    const body = Object.fromEntries(new FormData(event.currentTarget));
    body.quantity = Number(body.quantity);
    body.desiredBy ||= null;
    body.biddingDeadline = body.biddingDeadline ? new Date(body.biddingDeadline).toISOString() : null;
    body.version = draft?.version;
    setPending(true);
    setError(null);
    try {
      const response = await api(path, { token, body, method: id ? 'PUT' : 'POST' });
      navigate(requestPath(true) + '/' + response.request.id, { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  const localDeadline = draft?.biddingDeadline && new Date(draft.biddingDeadline);
  const deadlineInput = localDeadline && new Date(localDeadline - localDeadline.getTimezoneOffset() * 60000)
    .toISOString().slice(0, 19);

  return <section>
    <Link className="back" to={requestPath(true)}>← Về danh sách yêu cầu</Link>
    <h1>{draft ? 'Sửa bản nháp' : 'Tạo yêu cầu mua sỉ'}</h1>
    <form className={styles.form} onSubmit={save}>
      <label>Tiêu đề<input name="title" maxLength={160} required defaultValue={draft?.title} /></label>
      <label>Phân loại<select name="category" required defaultValue={draft?.category || ''}>
        <option value="">Chọn phân loại</option>
        {categories.current.data.map(item => <option key={item.slug} value={item.slug}>{item.name}</option>)}
      </select></label>
      <label>Số lượng<input name="quantity" type="number" min="1" max="2147483647" required defaultValue={draft?.quantity} /></label>
      <label>Đơn vị<input name="unit" maxLength={40} required defaultValue={draft?.unit} /></label>
      <label>Yêu cầu kỹ thuật<textarea name="spec" maxLength={10000} required rows={5} defaultValue={draft?.spec} /></label>
      <label>Ngày mong muốn<input name="desiredBy" type="date" defaultValue={draft?.desiredBy} /></label>
      <label>Hạn nhận báo giá (giờ địa phương)<input name="biddingDeadline" type="datetime-local" step="1" defaultValue={deadlineInput || ''} /></label>
      <label>Ghi chú<textarea name="note" maxLength={10000} defaultValue={draft?.note} /></label>
      {error && <p role="alert">{error.message} {Object.values(error.fieldErrors || {}).join(' ')}</p>}
      {error?.status === 409 && <button type="button" onClick={result.retry}>Tải lại bản nháp</button>}
      <button className="button" disabled={pending}>Lưu bản nháp</button>
    </form>
  </section>;
}
