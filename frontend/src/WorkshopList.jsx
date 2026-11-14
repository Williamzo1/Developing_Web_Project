import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApi } from './api/hooks';
import DataState from './components/DataState';
import Pagination from './components/Pagination';
import styles from './components/Form.module.css';

export default function WorkshopList() {
  const [params, setParams] = useSearchParams();
  const { current, retry } = useApi('/api/workshops?' + params);
  return <section>
    <h1>Hộ gia đình</h1>
    <form key={params.get('q') ?? ''} className={styles.form} onSubmit={event => {
      event.preventDefault();
      setParams({ q: new FormData(event.currentTarget).get('q').trim() });
    }}>
      <label>Tìm theo tên xưởng<input name="q" type="search" maxLength={160} defaultValue={params.get('q') ?? ''} /></label>
      <button className="button">Tìm xưởng</button>
    </form>
    {!current?.data ? <DataState current={current} retry={retry} /> :
      current.data.items.length === 0 ? <p className="notice">Không tìm thấy xưởng phù hợp.</p> :
      <ul className="grid">{current.data.items.map(workshop => <li className="card" key={workshop.slug}>
        <Link className="card-copy" to={'/ho-gia-dinh/' + encodeURIComponent(workshop.slug)}><h2>{workshop.name}</h2></Link>
      </li>)}</ul>}
    <Pagination data={current?.data} setPage={page => setParams(previous => {
      const next = new URLSearchParams(previous);
      next.set('page', page);
      return next;
    })} />
  </section>;
}
