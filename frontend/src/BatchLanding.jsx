import React from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import { batchKinds } from './Batches';
import { ProductPrice } from './ArtisanProductEdit';

export default function BatchLanding() {
  const { code } = useParams();
  const [params] = useSearchParams();
  const share = params.get('token');
  const { actor } = useAuth();
  const path = '/api/batches/' + encodeURIComponent(code);
  const result = useApi(path + (share ? '?' + new URLSearchParams({ token: share }) : ''));
  const { batch, milestones, steps } = result.current?.data || {};
  if (!batch) return <DataState {...result} missing="Không tìm thấy lô hàng." />;
  return <article>
    <Link className="back" to="/lo-hang">← Về danh sách lô</Link>
    <p className="eyebrow">{batch.code} · {batchKinds[batch.kind]}</p>
    <h1>{batch.name}</h1>
    <p>Số lượng: {batch.quantity} {batch.unit}</p><ProductPrice price={batch.price} />
    {batch.availableFrom && <p>Cung ứng dự kiến: {batch.availableFrom.split('-').reverse().join('/')}</p>}
    {batch.supplyCapacity && <p>Khả năng cung ứng: {batch.supplyCapacity}</p>}
    {batch.description && <p className="preserve-lines">{batch.description}</p>}
    <section className="section-gap"><h2>Mốc sản xuất</h2>
      {milestones.length === 0 ? <p>Chưa có mốc sản xuất.</p> :
        <ol>{milestones.map((item, i) => <li key={i}>
          <h3><time dateTime={item.happenedOn}>{item.happenedOn.split('-').reverse().join('/')}</time> · {item.title}</h3>
          {item.note && <p className="preserve-lines">{item.note}</p>}
        </li>)}</ol>}
    </section>
    <section className="section-gap"><h2>Quy trình của {batch.productName}</h2>
      {steps.length === 0 ? <p>Chưa có công đoạn công khai.</p> :
        <ol>{steps.map((step, i) => <li key={i}><strong>{step.name}</strong>
          {step.description && <p className="preserve-lines">{step.description}</p>}</li>)}</ol>}
    </section>
    <section className="section-gap"><h2>{batch.workshopName}</h2>
      {batch.familyHistory && <p className="preserve-lines">{batch.familyHistory}</p>}
      {batch.partnerNote && <p className="preserve-lines">{batch.partnerNote}</p>}
      <p><Link to={'/ho-gia-dinh/' + batch.workshopSlug}>Xem hồ sơ xưởng</Link></p>
      {(!actor || actor.role === 'BUYER') && <Link className="button" to="/mua-si/yeu-cau/moi">Gửi yêu cầu báo giá</Link>}
    </section>
  </article>;
}
