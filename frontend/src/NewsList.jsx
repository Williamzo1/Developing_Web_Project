import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import Pagination from './components/Pagination';
import styles from './components/Form.module.css';

export const newsKinds = { MATERIAL_PRICE: 'Giá nguyên liệu', POWER_CUT: 'Lịch cúp điện', POLICY: 'Chính sách', EVENT: 'Sự kiện và hội chợ' };

export default function NewsList({ admin = false }) {
  const [params, setParams] = useSearchParams();
  const { token } = useAuth();
  const base = admin ? '/quan-tri/tin-tuc' : '/tin-tuc';
  const result = useApi((admin ? '/api/admin/news' : '/api/news') + '?' + params, admin ? token : undefined);
  const data = result.current?.data;
  return <section>
    <h1>{admin ? 'Quản lý tin tức' : 'Tin tức cộng đồng'}</h1>
    {admin && <Link className="button" to={base + '/moi'}>Đăng tin</Link>}
    <div className={styles.form}><label>Loại tin<select value={params.get('kind') || ''} onChange={event =>
      setParams(event.target.value ? { kind: event.target.value } : {})}>
      <option value="">Tất cả loại tin</option>
      {Object.entries(newsKinds).map(([kind, label]) => <option key={kind} value={kind}>{label}</option>)}
    </select></label></div>
    {!data ? <DataState {...result} /> :
      data.items.length === 0 ? <p className="notice">Chưa có tin tức phù hợp.</p> :
      <ul className="grid">{data.items.map(news => <li key={news.id} className="card">
        <Link className="card-copy" to={base + '/' + (admin ? news.id : news.slug)}>
          <p className="eyebrow">{newsKinds[news.kind]}</p><h2>{news.title}</h2>
          {admin && <p>{news.hidden ? 'Đang ẩn' : 'Đang công khai'}</p>}
        </Link>
      </li>)}</ul>}
    <Pagination data={data} setPage={page => setParams(previous => {
      const next = new URLSearchParams(previous);
      next.set('page', page);
      return next;
    })} />
  </section>;
}
