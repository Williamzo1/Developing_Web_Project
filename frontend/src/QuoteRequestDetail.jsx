import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import { quotePath, quoteStates, requestPath } from './QuoteRequests';
import styles from './components/Form.module.css';

const money = new Intl.NumberFormat('vi-VN');
const dateTime = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
const offerStates = { SUBMITTED: 'Đã báo giá', ACCEPTED: 'Được chọn', NOT_SELECTED: 'Không được chọn' };

export default function QuoteRequestDetail({ buyer }) {
  const { id } = useParams();
  const { token } = useAuth();
  const path = quotePath(buyer) + '/' + encodeURIComponent(id);
  const result = useApi(path, token);
  const action = useMutation(token, result.retry);
  const navigate = useNavigate();
  const [confirmation, setConfirmation] = useState(null);
  const { request, quotes, events } = result.current?.data || {};
  if (!request) return <DataState {...result} missing="Không tìm thấy yêu cầu." />;

  const draft = request.status === 'DRAFT';
  const bidding = ['SENT', 'BIDDING'].includes(request.status);
  const biddingClosed = request.biddingDeadline && new Date(request.biddingDeadline) <= new Date();

  async function run(method, suffix, body) {
    if (await action.submit(path + suffix, method, body)) {
      setConfirmation(null);
      if (method === 'DELETE') navigate(requestPath(buyer), { replace: true });
    }
  }

  return <section>
    <Link className="back" to={requestPath(buyer)}>← Về danh sách yêu cầu</Link>
    <h1>{request.title}</h1>
    <p className="workshop">{quoteStates[request.status]}{request.targetName && ' · ' + request.targetName}</p>
    <p>Số lượng: {request.quantity} {request.unit}</p>
    {request.desiredBy && <p>Ngày mong muốn: {request.desiredBy.split('-').reverse().join('/')}</p>}
    {request.biddingDeadline && <p>Hạn nhận báo giá: {dateTime.format(new Date(request.biddingDeadline))}</p>}
    <h2>Yêu cầu kỹ thuật</h2><p className="preserve-lines">{request.spec}</p>
    {request.note && <p className="preserve-lines">{request.note}</p>}
    {buyer && draft && <>
      <div className={styles.actions}>
        <Link className="button" to={requestPath(true) + '/' + id + '/sua'}>Sửa bản nháp</Link>
        <button className="button" onClick={() => setConfirmation({ method: 'DELETE', suffix: '', text: 'Xóa bản nháp này?' })}>Xóa bản nháp</button>
      </div>
      <TargetForm submit={body => run('POST', '/send', body)} disabled={action.pending} />
    </>}
    {buyer && bidding && <button className="button" onClick={() => setConfirmation({
      method: 'POST', suffix: '/cancel', text: 'Hủy yêu cầu mua sỉ này?',
    })}>Hủy yêu cầu</button>}
    <section className={styles.subsection}>
      <h2>{buyer ? 'Báo giá nhận được' : 'Báo giá của xưởng'}</h2>
      {quotes.length === 0 && <p>Chưa có báo giá.</p>}
      {quotes.map(quote => <article className="card card-copy" key={quote.id}>
        <h3>{quote.workshop}</h3><p>{offerStates[quote.status]}</p>
        <p>Đơn giá: {money.format(quote.unitPrice)} ₫ · Tổng giá: {money.format(quote.totalPrice)} ₫</p>
        <p>Hoàn thành trong {quote.leadTimeDays} ngày</p>
        {quote.note && <p className="preserve-lines">{quote.note}</p>}
        {buyer && quote.contactEmail && <p>{quote.contactEmail}</p>}
        {buyer && quote.phone && <p>{quote.phone}</p>}
        {buyer && quote.facebookUrl && <a href={quote.facebookUrl}>Facebook của xưởng</a>}
        {buyer && request.status === 'BIDDING' && quote.status === 'SUBMITTED' &&
          <button className="button" onClick={() => setConfirmation({
            method: 'POST', suffix: '/quotes/' + quote.id + '/accept',
            body: { quoteUpdatedAt: quote.updatedAt }, text: 'Chấp nhận báo giá của ' + quote.workshop + '?',
          })}>Chấp nhận báo giá</button>}
      </article>)}
      {!buyer && bidding && (biddingClosed ? <p className="notice">Đã hết hạn nhận báo giá.</p> :
        <OfferForm quote={quotes[0]} submit={body => run('PUT', '/quote', body)} disabled={action.pending} />)}
      {!buyer && request.status === 'ACCEPTED' && <button className="button" disabled={action.pending}
        onClick={() => run('POST', '/start-production')}>Bắt đầu sản xuất</button>}
      {!buyer && request.status === 'IN_PRODUCTION' && <button className="button" disabled={action.pending}
        onClick={() => run('POST', '/deliver')}>Xác nhận đã giao</button>}
    </section>
    {confirmation && <section className="notice">
      <p>{confirmation.text}</p>
      <div className={styles.actions}>
        <button className="button" disabled={action.pending}
          onClick={() => run(confirmation.method, confirmation.suffix, confirmation.body)}>Xác nhận</button>
        <button className="button" disabled={action.pending} onClick={() => setConfirmation(null)}>Quay lại</button>
      </div>
    </section>}
    {action.error && <div className="notice"><p role="alert">{action.error.message}</p>
      <button className="button" onClick={() => { setConfirmation(null); result.retry(); }}>Tải lại yêu cầu</button></div>}
    <section className={styles.subsection}><h2>Lịch sử trạng thái</h2><ol>
      {events.map((event, index) => <li key={index}>{dateTime.format(new Date(event.createdAt))} ·{' '}
        {event.actorRole === 'BUYER' ? 'Bên mua' : 'Xưởng'} ·{' '}
        {event.fromStatus && quoteStates[event.fromStatus] + ' → '}{quoteStates[event.toStatus]}</li>)}
    </ol></section>
  </section>;
}

function TargetForm({ submit, disabled }) {
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [workshop, setWorkshop] = useState(null);
  const list = useApi('/api/workshops?' + new URLSearchParams({ q, page, size: 10 }));
  return <section className={styles.subsection}>
    <h2>Chỉ định một xưởng</h2>
    <form className={styles.form} onSubmit={event => {
      event.preventDefault();
      setQ(new FormData(event.currentTarget).get('q'));
      setPage(0);
    }}>
      <label>Tìm theo tên xưởng<input name="q" type="search" maxLength={160} /></label>
      <button className="button">Tìm xưởng</button>
    </form>
    {!list.current?.data ? <DataState {...list} /> : <>
      {list.current.data.items.length === 0 && <p>Không tìm thấy xưởng.</p>}
      <ul>{list.current.data.items.map(item => <li key={item.slug}>
        <button type="button" aria-pressed={item.slug === workshop?.slug} onClick={() => setWorkshop(item)}>{item.name}</button>
      </li>)}</ul>
      <div className={styles.actions}>
        <button type="button" disabled={list.current.data.first} onClick={() => setPage(page - 1)}>Trang trước</button>
        <button type="button" disabled={list.current.data.last} onClick={() => setPage(page + 1)}>Trang sau</button>
      </div>
    </>}
    {workshop && <div className="section-gap"><p>Xưởng đã chọn: {workshop.name}</p>
      <button className="button" disabled={disabled} onClick={() => submit({ workshop: workshop.slug })}>Gửi yêu cầu</button></div>}
  </section>;
}

function OfferForm({ quote, submit, disabled }) {
  return <form className={styles.form} onSubmit={event => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    submit({ unitPrice: Number(data.get('unitPrice')), totalPrice: Number(data.get('totalPrice')),
      leadTimeDays: Number(data.get('leadTimeDays')), note: data.get('note') });
  }}>
    <label>Đơn giá (₫)<input name="unitPrice" type="number" required min="0" max="9999999999999" defaultValue={quote?.unitPrice} /></label>
    <label>Tổng giá (₫)<input name="totalPrice" type="number" required min="0" max="9999999999999" defaultValue={quote?.totalPrice} /></label>
    <label>Thời gian hoàn thành (ngày)<input name="leadTimeDays" type="number" required min="1" max="2147483647" defaultValue={quote?.leadTimeDays} /></label>
    <label>Ghi chú<textarea name="note" maxLength={10000} defaultValue={quote?.note} /></label>
    <button className="button" disabled={disabled}>{quote ? 'Cập nhật báo giá' : 'Gửi báo giá'}</button>
  </form>;
}
