import React from 'react';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';

export default function AdminStats() {
  const { token } = useAuth();
  const result = useApi('/api/admin/stats', token);
  const data = result.current?.data;
  return <section><h1>Thống kê</h1>
    {!data ? <DataState {...result} /> : <dl className="grid">
      {[['workshops', 'Xưởng'], ['products', 'Sản phẩm'], ['batches', 'Lô hàng'], ['quoteRequests', 'Yêu cầu mua sỉ']]
        .map(([key, label]) => <div key={key} className="card card-copy"><dt>{label}</dt><dd>{data[key]}</dd></div>)}
    </dl>}
  </section>;
}
