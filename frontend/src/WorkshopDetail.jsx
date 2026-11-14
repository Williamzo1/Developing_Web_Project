import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import DataState from './components/DataState';

export default function WorkshopDetail() {
  const slug = encodeURIComponent(useParams().slug);
  const { current, retry } = useApi('/api/workshops/' + slug);
  const profile = current?.data;
  if (!profile) return <DataState current={current} retry={retry} missing="Không tìm thấy xưởng." />;
  return <article>
    <Link className="back" to="/ho-gia-dinh">← Về danh sách hộ</Link>
    <h1>{profile.name}</h1>
    {profile.approved && <p className="workshop">Đã xác minh bởi Ban quản lý</p>}
    {profile.description && <p className="preserve-lines">{profile.description}</p>}
    {profile.familyHistory && <section><h2>Lịch sử làm nghề</h2>
      <p className="preserve-lines">{profile.familyHistory}</p></section>}
    {(profile.address || profile.phone || profile.contactEmail || profile.facebookUrl) && <section>
      <h2>Liên hệ</h2>
      {profile.address && <p>{profile.address}</p>}
      {profile.phone && <p>{profile.phone}</p>}
      {profile.contactEmail && <p>{profile.contactEmail}</p>}
      {profile.facebookUrl && <p><a href={profile.facebookUrl}>Facebook của xưởng</a></p>}
    </section>}
    <p><Link className="button" to={'/san-pham?workshop=' + slug}>Xem sản phẩm của xưởng</Link></p>
    <Link className="button" to={'/lo-hang?workshop=' + slug}>Xem lô hàng của xưởng</Link>
  </article>;
}
