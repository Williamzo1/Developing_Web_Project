import React from 'react';
import { useApi } from './api/hooks';
import DataState from './components/DataState';

export default function ProductProcess({ slug }) {
  const result = useApi('/api/products/by-slug/' + encodeURIComponent(slug) + '/steps');
  const steps = result.current?.data;
  return <section className="section-gap">
    <h2>Quy trình sản xuất</h2>
    {!steps ? <DataState {...result} /> :
      steps.length === 0 ? <p className="notice">Xưởng chưa công bố quy trình.</p> :
      <ol>{steps.map((step, position) => <li key={position}>
        <h3>{step.name}</h3>
        {step.description && <p className="preserve-lines">{step.description}</p>}
      </li>)}</ol>}
  </section>;
}
