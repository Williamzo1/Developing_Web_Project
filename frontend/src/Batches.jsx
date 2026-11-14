import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import Pagination from './components/Pagination';
import styles from './components/Form.module.css';

export const batchKinds = { IN_STOCK: 'Có sẵn', MADE_TO_ORDER: 'Làm theo đơn' };

export default function Batches({ managed = false }) {
  const { token } = useAuth();
  const [params, setParams] = useSearchParams();
  const result = useApi((managed ? '/api/artisan/batches' : '/api/batches') + '?' + params, managed ? token : undefined);
  const data = result.current?.data;
  return <section>
    <h1>{managed ? 'Lô hàng của xưởng' : 'Lô hàng'}</h1>
    {managed ? <Link className="button" to="/xuong/lo-hang/moi">Thêm lô hàng</Link> :
      <form className={styles.form} key={params.toString()} onSubmit={event => {
        event.preventDefault();
        const values = new FormData(event.currentTarget);
        const next = new URLSearchParams();
        for (const [key, value] of values) if (value.trim()) next.set(key, value.trim());
        setParams(next);
      }}>
        <label>Mã sản phẩm (slug)<input name="product" defaultValue={params.get('product') || ''} /></label>
        <label>Mã xưởng (slug)<input name="workshop" defaultValue={params.get('workshop') || ''} /></label>
        <label>Loại lô<select name="kind" defaultValue={params.get('kind') || ''}>
          <option value="">Tất cả loại lô</option>
          {Object.entries(batchKinds).map(([kind, label]) => <option key={kind} value={kind}>{label}</option>)}
        </select></label>
        <button className="button">Lọc lô hàng</button>
        {params.size > 0 && <Link to="/lo-hang">Xóa bộ lọc</Link>}
      </form>}
    {!data ? <DataState {...result} /> :
      data.items.length === 0 ? <p className="notice">Chưa có lô hàng phù hợp.</p> :
      <ul className="grid">{data.items.map(batch => <li className="card" key={batch.id}>
        <Link className="card-copy" to={managed ? '/xuong/lo-hang/' + batch.id : '/lo/' + batch.publicCode}>
          <h2>{batch.name}</h2><p>{batch.workshopName} · {batchKinds[batch.kind]}</p>
          {managed && <p>{batch.hidden ? 'Ban quản lý đã ẩn' : batch.visibility === 'PUBLIC' ? 'Công khai' : 'Riêng tư'}</p>}
        </Link>
      </li>)}</ul>}
    <Pagination data={data} setPage={page => setParams(previous => {
      const next = new URLSearchParams(previous);
      next.set('page', page);
      return next;
    })} />
  </section>;
}
