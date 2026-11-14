import React from 'react';

export default function Pagination({ data, setPage }) {
  if (!data?.totalPages) return null;
  return <nav className="pagination" aria-label="Phân trang">
    <button type="button" disabled={data.first} onClick={() => setPage(data.page - 1)}>Trang trước</button>
    <span aria-live="polite">Trang {data.page + 1} / {data.totalPages}</span>
    <button type="button" disabled={data.last} onClick={() => setPage(data.page + 1)}>Trang sau</button>
  </nav>;
}
