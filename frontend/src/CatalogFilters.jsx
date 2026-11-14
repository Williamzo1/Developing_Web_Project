import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from './api/hooks';
import DataState from './components/DataState';
import styles from './components/Form.module.css';

export default function CatalogFilters({ params, setParams }) {
  const categories = useApi('/api/categories');
  const workshop = params.get('workshop') || '';
  const selected = useApi(workshop ? '/api/workshops/' + encodeURIComponent(workshop) : null);
  return <form key={params.toString()} className={styles.form} onSubmit={event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const next = new URLSearchParams();
    for (const [name, value] of form) if (value) next.set(name, value);
    setParams(next);
  }}>
    <label>Tìm theo tên sản phẩm<input name="q" type="search" maxLength={160} defaultValue={params.get('q') || ''} /></label>
    <label>Phân loại
      <select name="category" key={Boolean(categories.current?.data)} defaultValue={params.get('category') || ''}>
        <option value="">Tất cả phân loại</option>
        {categories.current?.data?.map(item => <option key={item.slug} value={item.slug}>{item.name}</option>)}
      </select>
    </label>
    {!categories.current?.data && <DataState current={categories.current} retry={categories.retry} />}
    <label htmlFor="catalog-workshop">Xưởng</label>
    <WorkshopPicker selected={selected.current?.data} slug={workshop} />
    <label>Sắp xếp<select name="sort" defaultValue={params.get('sort') || 'id'}>
      <option value="id">Mặc định</option><option value="name-asc">Tên A–Z</option><option value="name-desc">Tên Z–A</option>
    </select></label>
    <button className="button" disabled={!categories.current?.data}>Áp dụng</button>
    {params.size > 0 && <Link to="/san-pham">Xóa bộ lọc</Link>}
  </form>;
}

function WorkshopPicker({ selected, slug }) {
  const [page, setPage] = useState(0);
  const [choice, setChoice] = useState(null);
  const { current, retry } = useApi('/api/workshops?size=20&page=' + page);
  const options = current?.data?.items ?? [];
  const value = choice?.slug ?? slug;
  return <><select id="catalog-workshop" name="workshop" value={value} onChange={event =>
    setChoice(options.find(item => item.slug === event.target.value) || { slug: '' })}>
    <option value="">Tất cả xưởng</option>
    {value && !options.some(item => item.slug === value) && <option value={value}>{choice?.name || selected?.name || value}</option>}
    {options.map(item => <option key={item.slug} value={item.slug}>{item.name}</option>)}
  </select>
    {current?.error && <div role="alert">Chưa tải được xưởng. <button type="button" onClick={retry}>Thử lại</button></div>}
    {current?.data?.totalPages > 1 && <span className={styles.actions}>
      <button type="button" disabled={current.data.first} onClick={() => setPage(page - 1)}>Xưởng trước</button>
      <button type="button" disabled={current.data.last} onClick={() => setPage(page + 1)}>Xưởng sau</button>
    </span>}
  </>;
}
