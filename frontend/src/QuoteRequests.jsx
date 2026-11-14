import React from 'react';
import { Link } from 'react-router-dom';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';

export const quoteStates = {
  DRAFT: 'Nháp', SENT: 'Đã gửi', BIDDING: 'Đang nhận báo giá',
  ACCEPTED: 'Đã chọn báo giá', IN_PRODUCTION: 'Đang sản xuất',
  DELIVERED: 'Đã giao', CANCELLED: 'Đã hủy',
};

export const quotePath = buyer => '/api/' + (buyer ? 'buyer' : 'artisan') + '/quote-requests';
export const requestPath = buyer => (buyer ? '/mua-si' : '/xuong') + '/yeu-cau';

export default function QuoteRequests({ buyer }) {
  const { token } = useAuth();
  const result = useApi(quotePath(buyer), token);
  const requests = result.current?.data;
  return <section>
    <h1>{buyer ? 'Yêu cầu mua sỉ của tôi' : 'Yêu cầu gửi đến xưởng'}</h1>
    {buyer && <Link className="button" to={requestPath(buyer) + '/moi'}>Tạo yêu cầu</Link>}
    {!requests ? <DataState {...result} /> :
      requests.length === 0 ? <p className="notice">Chưa có yêu cầu mua sỉ.</p> :
      <ul className="grid">{requests.map(request => <li className="card" key={request.id}>
        <Link to={requestPath(buyer) + '/' + request.id} className="card-copy">
          <h2>{request.title}</h2><p>{quoteStates[request.status]}</p>
          {request.targetName && buyer && <p>{request.targetName}</p>}
        </Link>
      </li>)}</ul>}
  </section>;
}
