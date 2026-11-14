import React from 'react';

export default function DataState({ current, retry, missing = 'Không tìm thấy dữ liệu.' }) {
  if (!current) return <p className="notice" role="status">Đang tải dữ liệu…</p>;
  if (current.missing) return <p className="notice" role="status">{missing}</p>;
  return <div className="notice">
    <p role="alert">{current.error?.message || 'Chưa tải được dữ liệu.'}</p>
    <button className="button" type="button" onClick={retry}>Thử lại</button>
  </div>;
}
