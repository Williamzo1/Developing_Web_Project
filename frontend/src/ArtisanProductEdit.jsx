import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from './api/client';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import styles from './components/Form.module.css';

const path = '/api/artisan/products';
const number = new Intl.NumberFormat('vi-VN');
export function ProductPrice({ price }) {
  return <p>{price == null ? 'Liên hệ báo giá' : number.format(price) + ' ₫'}</p>;
}

export default function ArtisanProductEdit() {
  const { id } = useParams();
  const { token } = useAuth();
  const productData = useApi(id ? path + '/' + encodeURIComponent(id) : null, token);
  const categories = useApi('/api/categories');
  const navigate = useNavigate();
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [saved, setSaved] = useState(false);
  const update = useMutation(token);
  const product = productData.current?.data;
  if (id && !product) return <DataState {...productData} missing="Không tìm thấy sản phẩm." />;
  if (!categories.current?.data) return <DataState {...categories} />;

  async function save(event) {
    event.preventDefault();
    setSaved(false);
    const form = new FormData(event.currentTarget);
    const body = {
      name: form.get('name'), category: form.get('category'), description: form.get('description'),
      price: form.get('price') === '' ? null : Number(form.get('price')),
      priceVisible: form.has('priceVisible'), hidden: form.has('hidden'),
    };
    if (product) {
      setSaved(await update.submit(path + '/' + product.id, 'PUT', body));
      return;
    }
    setCreating(true);
    setCreateError(null);
    try {
      const created = await api(path, { token, method: 'POST', body });
      navigate('/xuong/san-pham/' + created.id, { replace: true });
    } catch (failure) {
      setCreateError(failure);
    } finally {
      setCreating(false);
    }
  }

  const error = createError || update.error;
  return <section>
    <Link className="back" to="/xuong/san-pham">← Về danh sách sản phẩm</Link>
    <h1>{product ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}</h1>
    {product?.adminHidden && <p className="notice">Ban quản lý đã ẩn sản phẩm này.</p>}
    <form className={styles.form} onSubmit={save} onChange={() => setSaved(false)}>
      <label>Tên sản phẩm<input name="name" required maxLength={160} defaultValue={product?.name} />
        <small>{error?.fieldErrors?.name}</small></label>
      <label>Phân loại<select name="category" required defaultValue={product?.category || ''}>
        <option value="">Chọn phân loại</option>
        {categories.current.data.map(item => <option key={item.slug} value={item.slug}>{item.name}</option>)}
      </select></label>
      <label>Mô tả<textarea name="description" rows={5} maxLength={10000} defaultValue={product?.description || ''} /></label>
      <label>Giá tham khảo (₫)<input type="number" name="price" min="0" max="9999999999999"
        step="1" defaultValue={product?.price ?? ''} /><small>{error?.fieldErrors?.price}</small></label>
      <label className={styles.checkbox}><input name="priceVisible" type="checkbox" defaultChecked={product?.priceVisible} />Công khai giá</label>
      <label className={styles.checkbox}><input name="hidden" type="checkbox" defaultChecked={product?.hidden} />Ẩn sản phẩm</label>
      {error && <p role="alert">{error.message}</p>}
      {saved && <p role="status">Đã lưu sản phẩm.</p>}
      <button className="button" disabled={creating || update.pending}>Lưu sản phẩm</button>
    </form>
    {product && <ProcessEditor product={product} token={token} key={product.id} />}
  </section>;
}

function ProcessEditor({ product, token }) {
  const [rows, setRows] = useState(() => product.steps.map(step => ({ ...step, key: crypto.randomUUID() })));
  const [saved, setSaved] = useState(false);
  const mutation = useMutation(token);

  function change(next) {
    setRows(next);
    setSaved(false);
  }

  function move(index, direction) {
    const next = [...rows];
    [next[index], next[index + direction]] = [next[index + direction], next[index]];
    change(next);
  }

  return <section className={styles.subsection}>
    <h2>Quy trình sản xuất</h2>
    <form className={styles.form} onSubmit={async event => {
      event.preventDefault();
      const steps = rows.map(({ name, description }) => ({ name, description }));
      setSaved(await mutation.submit(path + '/' + product.id + '/steps', 'PUT', { steps }));
    }}>
      {rows.length === 0 && <p>Chưa có công đoạn.</p>}
      {rows.map((step, index) => <fieldset key={step.key} className={styles.step}>
        <legend>Công đoạn {index + 1}</legend>
        <label>Tên<input required maxLength={160} value={step.name}
          onChange={event => change(rows.map(row => row.key === step.key ? { ...row, name: event.target.value } : row))} /></label>
        <label>Mô tả<textarea maxLength={10000} value={step.description || ''}
          onChange={event => change(rows.map(row => row.key === step.key ? { ...row, description: event.target.value } : row))} /></label>
        <div className={styles.actions}>
          <button className="button" type="button" disabled={!index} onClick={() => move(index, -1)}>Lên</button>
          <button className="button" type="button" disabled={index === rows.length - 1} onClick={() => move(index, 1)}>Xuống</button>
          <button className="button" type="button" onClick={() => change(rows.filter(row => row.key !== step.key))}>Bỏ công đoạn</button>
        </div>
      </fieldset>)}
      {mutation.error && <p role="alert">{mutation.error.message}{' '}
        {Object.values(mutation.error.fieldErrors || {}).join(' ')}</p>}
      {saved && <p role="status">Đã lưu quy trình.</p>}
      <div className={styles.actions}>
        <button type="button" className="button" disabled={rows.length >= 100 || mutation.pending}
          onClick={() => change([...rows, { key: crypto.randomUUID(), name: '', description: '' }])}>Thêm công đoạn</button>
        <button className="button" disabled={mutation.pending}>Lưu quy trình</button>
      </div>
    </form>
  </section>;
}
