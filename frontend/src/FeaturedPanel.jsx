import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import styles from './components/Form.module.css';

const fields = [
  ['workshop', 'Xưởng tiêu biểu', 'workshopId', '/ho-gia-dinh/'],
  ['product', 'Sản phẩm nổi bật', 'productId', '/san-pham/'],
  ['batch', 'Lô hàng nổi bật', 'batchId', '/lo/'],
];

export function HomeFeatured() {
  const result = useApi('/api/home');
  const data = result.current?.data;
  if (!data) return <DataState {...result} />;
  if (!fields.some(([key]) => data[key])) return null;
  return <section className="section-gap"><h2>Nổi bật</h2><ul className="grid">
    {fields.map(([key, label, , prefix]) => {
      const item = data[key];
      return item && <li className="card" key={key}><Link to={prefix + encodeURIComponent(item.code)}>
        {item.imageUrl && <img src={item.imageUrl} alt={item.name} width="640" height="480" loading="lazy" />}
        <div className="card-copy"><p className="eyebrow">{label}</p><h3>{item.name}</h3></div>
      </Link></li>;
    })}
  </ul></section>;
}

export default function FeaturedPanel() {
  const { token } = useAuth();
  const path = '/api/admin/featured';
  const selected = useApi(path, token);
  const update = useMutation(token);
  const moderate = useMutation(token);
  const [saved, setSaved] = useState(false);
  const [hidden, setHidden] = useState(false);

  return <section><h1>Nội dung nổi bật</h1>
    {!selected.current?.data ? <DataState {...selected} /> :
      <form className={styles.form} onSubmit={async event => {
        event.preventDefault();
        const body = Object.fromEntries(new FormData(event.currentTarget));
        for (const key of Object.keys(body)) body[key] = body[key] ? Number(body[key]) : null;
        setSaved(await update.submit(path, 'PUT', body));
      }} onChange={() => setSaved(false)}>
        {fields.map(([, label, name]) => <label key={name}>{label} (ID)
          <input name={name} type="number" min="1" max={Number.MAX_SAFE_INTEGER} defaultValue={selected.current.data[name] ?? ''} />
        </label>)}
        <p>Để trống mục nào nếu không muốn hiển thị mục đó trên trang chủ.</p>
        {update.error && <p role="alert">{update.error.message}</p>}
        {saved && <p role="status">Đã lưu nội dung nổi bật.</p>}
        <button className="button" disabled={update.pending}>Lưu lựa chọn</button>
      </form>}
    <section className="section-gap"><h2>Ẩn nội dung vi phạm</h2>
      <form className={styles.form} onSubmit={async event => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        if (window.confirm('Ẩn nội dung này khỏi các trang công khai?')) {
          setHidden(await moderate.submit('/api/admin/' + data.get('kind') + '/' + data.get('id') + '/hide', 'POST'));
        }
      }} onChange={() => setHidden(false)}>
        <label>Loại nội dung<select name="kind"><option value="products">Sản phẩm</option><option value="batches">Lô hàng</option></select></label>
        <label>ID<input name="id" type="number" min="1" max={Number.MAX_SAFE_INTEGER} required /></label>
        {moderate.error && <p role="alert">{moderate.error.message}</p>}
        {hidden && <p role="status">Đã ẩn nội dung.</p>}
        <button className="button" disabled={moderate.pending}>Ẩn khỏi trang công khai</button>
      </form>
    </section>
  </section>;
}
