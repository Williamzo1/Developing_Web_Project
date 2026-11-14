import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import Pagination from './components/Pagination';

export default function ArtisanProducts() {
  const { token } = useAuth();
  const [params, setParams] = useSearchParams();
  const { current, retry } = useApi('/api/artisan/products?' + params, token);
  return <section>
    <h1>Sản phẩm của xưởng</h1>
    <Link className="button" to="/xuong/san-pham/moi">Thêm sản phẩm</Link>
    {!current?.data ? <DataState current={current} retry={retry} /> :
      current.data.items.length === 0 ? <p className="notice">Xưởng chưa có sản phẩm.</p> :
      <ul className="grid">{current.data.items.map(product => <li className="card" key={product.id}>
        <Link className="card-copy" to={'/xuong/san-pham/' + product.id}><h2>{product.name}</h2>
          <p>{product.adminHidden ? 'Ban quản lý đã ẩn' : product.hidden ? 'Đang ẩn' : 'Đang hiển thị'}</p>
        </Link>
      </li>)}</ul>}
    <Pagination data={current?.data} setPage={page => setParams({ page })} />
  </section>;
}
