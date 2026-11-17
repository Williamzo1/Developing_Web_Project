import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Link, NavLink, Route, Routes, useParams } from 'react-router-dom';
import './styles.css';

function useCatalog(path) {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState(null);
  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const response = await fetch(path, {
          headers: { Accept: 'application/json' },
          signal: controller.signal,
        });
        if (response.status === 404) {
          setResult({ path, attempt, missing: true });
          return;
        }
        if (!response.ok) throw new Error('API request failed');
        const data = await response.json();
        setResult({ path, attempt, data });
      } catch (error) {
        if (error.name !== 'AbortError') setResult({ path, attempt, error: true });
      }
    }
    load();
    return () => controller.abort();
  }, [path, attempt]);
  return {
    current: result?.path === path && result.attempt === attempt ? result : null,
    retry: () => setAttempt(value => value + 1),
  };
}

function Home() {
  return (
    <section className="hero">
      <div className="hero-copy">
        <p className="eyebrow">BÁT TRÀNG · TRIỂN LÃM SỐ</p>
        <h1>Gốm làng nghề</h1>
        <p>Khám phá những dáng gốm, màu men và câu chuyện từ các xưởng thủ công Việt.</p>
        <Link className="button" to="/san-pham">Khám phá danh mục <span aria-hidden="true">→</span></Link>
      </div>
      <img src="/images/binh-men-lam.svg" alt="Minh họa bình gốm men lam" width="640" height="480" />
    </section>
  );
}

function ResultState({ current, retry }) {
  if (!current) return <p className="notice" role="status">Đang tải dữ liệu…</p>;
  if (current.missing) return <p className="notice" role="status">Không tìm thấy sản phẩm.</p>;
  return (
    <div className="notice">
      <p role="alert">Chưa tải được dữ liệu. Vui lòng thử lại.</p>
      <button className="button" type="button" onClick={retry}>Thử lại</button>
    </div>
  );
}

function ProductImage({ product }) {
  const [failed, setFailed] = useState(false);
  if (!product.imageUrl || failed) {
    return <div className="image-placeholder">Chưa có ảnh minh họa</div>;
  }
  return <img src={product.imageUrl} alt={product.name} width="640" height="480"
    onError={() => setFailed(true)} />;
}

function Catalog() {
  const [page, setPage] = useState(0);
  const { current, retry } = useCatalog(`/api/products/page?page=${page}&size=12`);
  return (
    <section>
      <p className="eyebrow">BỘ SƯU TẬP</p>
      <h1>Danh mục sản phẩm</h1>
      {!current?.data ? <ResultState current={current} retry={retry} /> :
        current.data.items.length === 0 ? <p className="notice" role="status">Danh mục hiện chưa có sản phẩm.</p> :
        <ul className="grid">
          {current.data.items.map(product => (
            <li className="card" key={product.id}>
              <Link to={'/san-pham/' + product.id}>
                <ProductImage product={product} />
                <div className="card-copy">
                  <p className="eyebrow">{product.category}</p>
                  <h2>{product.name}</h2>
                  <p>{product.workshop}</p>
                </div>
              </Link>
            </li>
          ))}
        </ul>}
      {current?.data && current.data.totalPages > 0 && (
        <nav className="pagination" aria-label="Phân trang sản phẩm">
          <button type="button" disabled={current.data.first} onClick={() => setPage(p => p - 1)}>Trang trước</button>
          <span aria-live="polite">Trang {page + 1} / {current.data.totalPages} · {current.data.totalElements} sản phẩm</span>
          <button type="button" disabled={current.data.last} onClick={() => setPage(p => p + 1)}>Trang sau</button>
        </nav>
      )}
    </section>
  );
}

function Detail() {
  const { id } = useParams();
  const { current, retry } = useCatalog('/api/products/' + encodeURIComponent(id));
  const product = current?.data;
  return (
    <section>
      <Link className="back" to="/san-pham">← Về danh mục</Link>
      {!product ? <ResultState current={current} retry={retry} /> :
        <article className="detail">
          <ProductImage key={product.id} product={product} />
          <div>
            <p className="eyebrow">{product.category}</p>
            <h1>{product.name}</h1>
            <p className="workshop">{product.workshop}</p>
            <p>{product.description || 'Sản phẩm chưa có mô tả.'}</p>
          </div>
        </article>}
    </section>
  );
}

function App() {
  return (
    <BrowserRouter>
      <header>
        <div className="shell nav">
          <Link className="brand" to="/">vCraft<span>.</span></Link>
          <nav aria-label="Điều hướng chính">
            <NavLink to="/" end>Trang chủ</NavLink>
            <NavLink to="/san-pham">Danh mục</NavLink>
          </nav>
        </div>
      </header>
      <main className="shell">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/san-pham" element={<Catalog />} />
          <Route path="/san-pham/:id" element={<Detail />} />
          <Route path="*" element={<section><h1>Không tìm thấy trang</h1><Link to="/">Về trang chủ</Link></section>} />
        </Routes>
      </main>
      <footer className="shell">vCraft · Bản mẫu INT3306 · Sản phẩm và xưởng minh họa</footer>
    </BrowserRouter>
  );
}

createRoot(document.getElementById('root')).render(<App />);
