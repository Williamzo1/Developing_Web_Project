import React, { useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from './api/client';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import { batchKinds } from './Batches';
import styles from './components/Form.module.css';

const inputs = [
  ['code', 'Mã lô', 120, true], ['name', 'Tên lô', 160, true], ['unit', 'Đơn vị', 40, true],
  ['supplyCapacity', 'Khả năng cung ứng', 200, false],
];

export default function BatchEdit() {
  const { id } = useParams();
  const { token } = useAuth();
  const path = '/api/artisan/batches' + (id ? '/' + encodeURIComponent(id) : '');
  const result = useApi(id ? path : null, token);
  const navigate = useNavigate();
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState(null);
  const { batch, milestones } = result.current?.data || {};
  if (id && !batch) return <DataState {...result} missing="Không tìm thấy lô hàng." />;

  async function save(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const body = Object.fromEntries(form);
    body.productId = Number(body.productId);
    body.quantity = Number(body.quantity);
    body.price = body.price === '' ? null : Number(body.price);
    body.priceVisible = form.has('priceVisible');
    body.availableFrom ||= null;
    setSaving(true);
    setSaved(false);
    setError(null);
    try {
      const response = await api(path, { token, method: id ? 'PUT' : 'POST', body });
      if (id) { setSaved(true); result.retry(); }
      else navigate('/xuong/lo-hang/' + response.batch.id, { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setSaving(false);
    }
  }

  return <section>
    <Link className="back" to="/xuong/lo-hang">← Về danh sách lô</Link>
    <h1>{batch ? 'Sửa lô hàng' : 'Thêm lô hàng'}</h1>
    <form className={styles.form} onSubmit={save} onChange={() => setSaved(false)}>
      <ProductChoice selected={batch && { id: batch.productId, name: batch.productName }} token={token} />
      {inputs.map(([name, label, maxLength, required]) => <label key={name}>{label}
        <input name={name} maxLength={maxLength} required={required} defaultValue={batch?.[name] || ''} /></label>)}
      <label>Loại lô<select name="kind" defaultValue={batch?.kind}>
        {Object.entries(batchKinds).map(([kind, label]) => <option key={kind} value={kind}>{label}</option>)}
      </select></label>
      <label>Số lượng<input name="quantity" type="number" min="1" max="2147483647" required defaultValue={batch?.quantity} /></label>
      <label>Giá tham khảo (₫)<input name="price" type="number" min="0" max="9999999999999" defaultValue={batch?.price ?? ''} /></label>
      <label className={styles.checkbox}><input type="checkbox" name="priceVisible" defaultChecked={batch?.priceVisible} />Công khai giá</label>
      <label>Cung ứng dự kiến<input name="availableFrom" type="date" defaultValue={batch?.availableFrom || ''} /></label>
      <label>Mô tả<textarea name="description" maxLength={10000} defaultValue={batch?.description || ''} /></label>
      <label>Hộ cùng tham gia<textarea name="partnerNote" maxLength={10000} defaultValue={batch?.partnerNote || ''} /></label>
      <label>Hiển thị<select name="visibility" defaultValue={batch?.visibility || 'PRIVATE'}>
        <option value="PRIVATE">Riêng tư</option><option value="PUBLIC">Công khai</option>
      </select></label>
      {error && <p role="alert">{error.message} {Object.values(error.fieldErrors || {}).join(' ')}</p>}
      {saved && <p role="status">Đã lưu lô hàng.</p>}
      <button className="button" disabled={saving}>Lưu lô hàng</button>
    </form>
    {batch && <>
      <SharePanel batch={batch} path={path} token={token} />
      <MilestoneForm milestones={milestones} path={path} token={token} />
    </>}
  </section>;
}

function ProductChoice({ selected, token }) {
  const [page, setPage] = useState(0);
  const [choice, setChoice] = useState(selected);
  const result = useApi('/api/artisan/products?size=20&page=' + page, token);
  const data = result.current?.data;
  return <>
    <label htmlFor="batch-product">Sản phẩm</label>
      <select id="batch-product" name="productId" value={choice?.id ?? ''} required onChange={event => {
        setChoice(data?.items.find(item => String(item.id) === event.target.value));
      }}>
        <option value="">Chọn sản phẩm</option>
        {choice && !data?.items.some(item => item.id === choice.id) && <option value={choice.id}>{choice.name}</option>}
        {data?.items.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}
      </select>
    {!data ? <DataState {...result} /> : <div className={styles.actions}>
      <button type="button" disabled={data.first} onClick={() => setPage(page - 1)}>Sản phẩm trước</button>
      <button type="button" disabled={data.last} onClick={() => setPage(page + 1)}>Sản phẩm sau</button>
    </div>}
  </>;
}

function SharePanel({ batch, path, token }) {
  const [share, setShare] = useState(null);
  const [error, setError] = useState(null);
  const [pending, setPending] = useState(false);
  const shareUrl = '/lo/' + batch.publicCode;
  async function manage(method) {
    setError(null);
    setPending(true);
    try {
      const result = await api(path + '/share-token', { token, method });
      setShare(result ? new URL(shareUrl + '?' + new URLSearchParams({ token: result.token }), location.origin).href : '');
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return <section className={styles.subsection}><h2>Liên kết lô hàng</h2>
    {batch.hidden ? <p>Ban quản lý đã ẩn lô này.</p> : batch.visibility === 'PUBLIC' ?
      <Link to={shareUrl}>Mở trang công khai</Link> : <>
        <div className={styles.actions}>
          <button className="button" disabled={pending} onClick={() => manage('POST')}>Tạo liên kết riêng</button>
          <button className="button" disabled={pending} onClick={() => manage('DELETE')}>Thu hồi liên kết riêng</button>
        </div>
        {share && <p className="preserve-lines"><a href={share} rel="noreferrer">{share}</a></p>}
        {share === '' && <p role="status">Đã thu hồi liên kết riêng.</p>}
        {error && <p role="alert">{error.message}</p>}
      </>}
  </section>;
}

function MilestoneForm({ milestones, path, token }) {
  const nextKey = useRef(milestones.length);
  const [items, setItems] = useState(() => milestones.map((item, index) => ({ ...item, key: index })));
  const [saved, setSaved] = useState(false);
  const mutation = useMutation(token);
  function edit(key, field, value) {
    setSaved(false);
    setItems(previous => previous.map(item => item.key === key ? { ...item, [field]: value } : item));
  }
  return <section className={styles.subsection}>
    <h2>Mốc sản xuất</h2>
    <form className={styles.form} onSubmit={async event => {
      event.preventDefault();
      setSaved(await mutation.submit(path + '/milestones', 'PUT', {
        items: items.map(({ title, note, happenedOn }) => ({ title, note, happenedOn })),
      }));
    }}>
      {items.length === 0 && <p>Chưa có mốc sản xuất.</p>}
      {items.map(item => <fieldset key={item.key} className={styles.step}>
        <label>Tên mốc<input value={item.title} required maxLength={160} onChange={event => edit(item.key, 'title', event.target.value)} /></label>
        <label>Ngày<input type="date" value={item.happenedOn} required onChange={event => edit(item.key, 'happenedOn', event.target.value)} /></label>
        <label>Ghi chú<textarea value={item.note || ''} maxLength={10000} onChange={event => edit(item.key, 'note', event.target.value)} /></label>
        <button type="button" className="button" onClick={() => {
          setSaved(false);
          setItems(previous => previous.filter(row => row.key !== item.key));
        }}>Bỏ mốc</button>
      </fieldset>)}
      {mutation.error && <p role="alert">{mutation.error.message} {Object.values(mutation.error.fieldErrors || {}).join(' ')}</p>}
      {saved && <p role="status">Đã lưu mốc sản xuất.</p>}
      <div className={styles.actions}>
        <button type="button" className="button" disabled={items.length >= 100} onClick={() => {
          const key = nextKey.current++;
          setSaved(false);
          setItems(previous => [...previous, { key, title: '', note: '', happenedOn: '' }]);
        }}>Thêm mốc</button>
        <button className="button" disabled={mutation.pending}>Lưu các mốc</button>
      </div>
    </form>
  </section>;
}
