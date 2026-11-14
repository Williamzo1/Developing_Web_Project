import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import DataState from './components/DataState';
import { newsKinds } from './NewsList';

const dateTime = new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' });

export default function NewsDetail() {
  const { slug } = useParams();
  const result = useApi('/api/news/' + encodeURIComponent(slug));
  const news = result.current?.data;
  if (!news) return <DataState {...result} missing="Không tìm thấy bài viết." />;
  return <article>
    <Link className="back" to="/tin-tuc">← Về danh sách tin</Link>
    <p className="eyebrow">{newsKinds[news.kind]}</p><h1>{news.title}</h1>
    {news.eventStart && <p>Thời gian: {dateTime.format(new Date(news.eventStart))}
      {news.eventEnd && ' – ' + dateTime.format(new Date(news.eventEnd))}</p>}
    {news.eventPlace && <p>Địa điểm: {news.eventPlace}</p>}
    <p className="preserve-lines">{news.body}</p>
  </article>;
}
