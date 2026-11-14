import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Link, NavLink, Outlet, Route, Routes, useParams, useSearchParams } from 'react-router-dom';
import './styles.css';

import { useApi } from './api/hooks';
import AccountNav from './auth/AccountNav';
import { AuthProvider, roleNames, useAuth } from './auth/AuthContext';
import Login from './auth/Login';
import RequireRole from './auth/RequireRole';
import DataState from './components/DataState';
import WorkspaceLayout from './components/WorkspaceLayout';
import AdminWorkshops from './AdminWorkshops';
import CatalogFilters from './CatalogFilters';
import Pagination from './components/Pagination';
import WorkshopList from './WorkshopList';
import WorkshopDetail from './WorkshopDetail';
import WorkshopEdit from './WorkshopEdit';
import ArtisanProducts from './ArtisanProducts';
import ArtisanProductEdit, { ProductPrice } from './ArtisanProductEdit';
import ProductProcess from './ProductProcess';
import QuoteDraft from './QuoteDraft';
import QuoteRequestDetail from './QuoteRequestDetail';
import QuoteRequests from './QuoteRequests';
import Batches from './Batches';
import BatchEdit from './BatchEdit';
import BatchLanding from './BatchLanding';
import NewsList from './NewsList';
import NewsDetail from './NewsDetail';
import NewsEdit from './NewsEdit';
import AdminStats from './AdminStats';
import FeaturedPanel, { HomeFeatured } from './FeaturedPanel';
import ChangePassword from './ChangePassword';
import WorkshopMap from './WorkshopMap';

function Home() {
  return (
    <><section className="hero">
      <div className="hero-copy">
        <p className="eyebrow">BÁT TRÀNG · TRIỂN LÃM SỐ</p>
        <h1>Gốm làng nghề</h1>
        <p>Qua nhiều thế hệ, các gia đình ở Bát Tràng lưu giữ nghề làm gốm bên sông Hồng. Từ tạo dáng, vẽ men đến nung lò, mỗi xưởng góp phần gìn giữ bản sắc làng nghề.</p>
        <Link className="button" to="/san-pham">Khám phá danh mục <span aria-hidden="true">→</span></Link>
      </div>
      <img src="/images/binh-men-lam.svg" alt="Minh họa bình gốm men lam" width="640" height="480" />
    </section><HomeFeatured /></>
  );
}

function ProductImage({ product }) {
  const [failed, setFailed] = useState(false);
  if (!product.imageUrl || failed) {
    return <div className="image-placeholder">Chưa có ảnh minh họa</div>;
  }
  return <img src={product.imageUrl} alt={product.name} width="640" height="480"
    loading="lazy" onError={() => setFailed(true)} />;
}

function Catalog() {
  const [params, setParams] = useSearchParams();
  const { current, retry } = useApi('/api/products/page?' + params);
  return (
    <section>
      <p className="eyebrow">BỘ SƯU TẬP</p>
      <h1>Danh mục sản phẩm</h1>
      <CatalogFilters params={params} setParams={setParams} />
      {!current?.data ? <DataState current={current} retry={retry} missing="Không tìm thấy sản phẩm." /> :
        current.data.items.length === 0 ? <p className="notice" role="status">Danh mục hiện chưa có sản phẩm.</p> :
        <ul className="grid">
          {current.data.items.map(product => (
            <li className="card" key={product.id}>
              <Link to={'/san-pham/' + encodeURIComponent(product.slug)}>
                <ProductImage product={product} />
                <div className="card-copy">
                  <p className="eyebrow">{product.category}</p>
                  <h2>{product.name}</h2>
                  <p>{product.workshop}</p>
                  <ProductPrice price={product.price} />
                </div>
              </Link>
            </li>
          ))}
        </ul>}
      <Pagination data={current?.data} setPage={page => setParams(previous => {
        const next = new URLSearchParams(previous);
        next.set('page', page);
        return next;
      })} />
    </section>
  );
}

function Detail() {
  const { slug } = useParams();
  const legacy = /^\d+$/.test(slug);
  const { current, retry } = useApi('/api/products/' + (legacy ? '' : 'by-slug/') + encodeURIComponent(slug));
  const product = current?.data;
  return (
    <section>
      <Link className="back" to="/san-pham">← Về danh mục</Link>
      {!product ? <DataState current={current} retry={retry} missing="Không tìm thấy sản phẩm." /> :
        <article><div className="detail">
          <ProductImage key={product.id} product={product} />
          <div>
            <p className="eyebrow">{product.category}</p>
            <h1>{product.name}</h1>
            <p className="workshop"><Link to={'/ho-gia-dinh/' + encodeURIComponent(product.workshopSlug)}>{product.workshop}</Link></p>
            <ProductPrice price={product.price} />
            <p>{product.description || 'Sản phẩm chưa có mô tả.'}</p>
          </div>
        </div><ProductProcess slug={product.slug} />
          <Link className="button" to={'/lo-hang?product=' + encodeURIComponent(product.slug)}>Xem lô hàng</Link>
        </article>}
    </section>
  );
}

function PublicLayout() {
  return <>
    <header><div className="shell nav">
      <Link className="brand" to="/">vCraft<span>.</span></Link>
      <nav aria-label="Điều hướng chính">
        <NavLink to="/" end>Trang chủ</NavLink>
        <NavLink to="/ho-gia-dinh">Hộ gia đình</NavLink>
        <NavLink to="/san-pham">Danh mục</NavLink>
        <NavLink to="/lo-hang">Lô hàng</NavLink>
        <NavLink to="/ban-do">Bản đồ</NavLink>
        <NavLink to="/tin-tuc">Tin tức</NavLink>
        <AccountNav />
      </nav>
    </div></header>
    <main className="shell"><Outlet /></main>
    <footer className="shell">vCraft · Bát Tràng</footer>
  </>;
}

function Account() {
  const { actor } = useAuth();
  return <section><h1>{roleNames[actor.role]}</h1><p>Mã tài khoản: {actor.id}</p></section>;
}

function App() {
  return <BrowserRouter><AuthProvider><Routes>
    <Route element={<PublicLayout />}>
      <Route index element={<Home />} />
      <Route path="/san-pham" element={<Catalog />} />
      <Route path="/san-pham/:slug" element={<Detail />} />
      <Route path="/ho-gia-dinh" element={<WorkshopList />} />
      <Route path="/ho-gia-dinh/:slug" element={<WorkshopDetail />} />
      <Route path="/lo-hang" element={<Batches />} />
      <Route path="/lo/:code" element={<BatchLanding />} />
      <Route path="/ban-do" element={<WorkshopMap />} />
      <Route path="/tin-tuc" element={<NewsList />} />
      <Route path="/tin-tuc/:slug" element={<NewsDetail />} />
      <Route path="/dang-nhap" element={<Login />} />
      <Route path="*" element={<section><h1>Không tìm thấy trang</h1><Link to="/">Về trang chủ</Link></section>} />
    </Route>
    <Route element={<RequireRole role="BUYER" />}>
      <Route path="/mua-si" element={<WorkspaceLayout label="Mua sỉ" links={[['/mua-si', 'Tài khoản'], ['/mua-si/yeu-cau', 'Yêu cầu mua sỉ']]} />}>
        <Route index element={<Account />} />
        <Route path="mat-khau" element={<ChangePassword />} />
        <Route path="yeu-cau" element={<QuoteRequests buyer />} />
        <Route path="yeu-cau/moi" element={<QuoteDraft />} />
        <Route path="yeu-cau/:id" element={<QuoteRequestDetail buyer />} />
        <Route path="yeu-cau/:id/sua" element={<QuoteDraft />} />
      </Route>
    </Route>
    <Route element={<RequireRole role="ARTISAN" />}>
      <Route path="/xuong" element={<WorkspaceLayout label="Quản lý xưởng" links={[['/xuong', 'Tài khoản'], ['/xuong/ho-so', 'Hồ sơ xưởng'], ['/xuong/san-pham', 'Sản phẩm'], ['/xuong/lo-hang', 'Lô hàng'], ['/xuong/yeu-cau', 'Yêu cầu nhận được']]} />}>
        <Route index element={<Account />} />
        <Route path="mat-khau" element={<ChangePassword />} />
        <Route path="lo-hang" element={<Batches managed />} />
        <Route path="lo-hang/moi" element={<BatchEdit key="new-batch" />} />
        <Route path="lo-hang/:id" element={<BatchEdit />} />
        <Route path="ho-so" element={<WorkshopEdit />} />
        <Route path="yeu-cau" element={<QuoteRequests buyer={false} />} />
        <Route path="yeu-cau/:id" element={<QuoteRequestDetail buyer={false} />} />
        <Route path="san-pham" element={<ArtisanProducts />} />
        <Route path="san-pham/moi" element={<ArtisanProductEdit key="new-product" />} />
        <Route path="san-pham/:id" element={<ArtisanProductEdit />} />
      </Route>
    </Route>
    <Route element={<RequireRole role="ADMIN" />}>
      <Route path="/quan-tri" element={<WorkspaceLayout label="Quản trị" links={[
        ['/quan-tri', 'Thống kê'], ['/quan-tri/ho-gia-dinh', 'Quản lý hộ gia đình'], ['/quan-tri/tin-tuc', 'Tin tức'], ['/quan-tri/noi-bat', 'Nội dung nổi bật'],
      ]} />}>
        <Route index element={<AdminStats />} />
        <Route path="mat-khau" element={<ChangePassword />} />
        <Route path="noi-bat" element={<FeaturedPanel />} />
        <Route path="tin-tuc" element={<NewsList admin />} />
        <Route path="tin-tuc/moi" element={<NewsEdit />} />
        <Route path="tin-tuc/:id" element={<NewsEdit />} />
        <Route path="ho-gia-dinh" element={<AdminWorkshops />} />
      </Route>
    </Route>
  </Routes></AuthProvider></BrowserRouter>;
}

createRoot(document.getElementById('root')).render(<App />);
