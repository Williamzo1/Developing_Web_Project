# vCraft — File sẵn sàng copy vào repo
# Copy trực tiếp, không cần đọc thêm tài liệu khác

---

## 1. BUG FIX — WorkshopService.provision() dùng sai slug

**Vấn đề:** Code hiện tại gọi `createPending(input.username(), input.workshopName())` — dùng `username` làm slug. Sai.

**Sửa** `backend/src/main/java/vn/vcraft/backend/service/WorkshopService.java`:

Thay dòng:
```java
var workshop = workshops.createPending(input.username(), input.workshopName().strip());
```
Thành:
```java
String slug = vn.vcraft.backend.validation.SlugUtils.uniqueSlug(
    input.workshopName(), s -> workshops.existsBySlug(s));
var workshop = workshops.createPending(slug, input.workshopName().strip());
```

Thêm vào `WorkshopRepository.java` (sau method `updateContact`):
```java
public boolean existsBySlug(String slug) {
    var count = db.queryForObject(
        "SELECT COUNT(*) FROM workshops WHERE slug = ?", Integer.class, slug);
    return count != null && count > 0;
}
```

---

## 2. SecurityConfig.java — Phiên bản hoàn chỉnh

Thay toàn bộ nội dung file `backend/src/main/java/vn/vcraft/backend/config/SecurityConfig.java`:

```java
package vn.vcraft.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.security.BearerTokenFilter;
import vn.vcraft.backend.security.SecurityErrorWriter;

import java.util.List;

@Configuration
class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    BCryptPasswordEncoder passwords() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain security(
            HttpSecurity http, AccountRepository accounts,
            SecurityErrorWriter errors,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                    errors.write(req, res, 401, "UNAUTHENTICATED", "Vui lòng đăng nhập."))
                .accessDeniedHandler((req, res, ex) ->
                    errors.write(req, res, 403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác.")))
            .authorizeHttpRequests(auth -> auth
                // Public GET
                .requestMatchers(HttpMethod.GET,
                    "/api/health", "/api/health/**",
                    "/api/products/**",
                    "/api/categories",
                    "/api/tags",
                    "/api/workshops", "/api/workshops/**",
                    "/api/batches", "/api/batches/**",
                    "/api/news", "/api/news/**",
                    "/api/home",
                    "/api/quote-tags",
                    "/api/quote-categories")
                    .permitAll()
                // Public POST (auth + register)
                .requestMatchers(HttpMethod.POST,
                    "/api/login",
                    "/api/register",
                    "/api/register/verify-email",
                    "/api/register/resend-otp")
                    .permitAll()
                .requestMatchers("/error").permitAll()
                // Role-based
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/buyer/**").hasRole("BUYER")
                .requestMatchers("/api/artisan/**").hasRole("ARTISAN")
                // Mọi request còn lại cần đăng nhập (vd: POST /api/media)
                .anyRequest().authenticated())
            .addFilterBefore(
                new BearerTokenFilter(accounts),
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 3. AccountRepository.java — Phiên bản hoàn chỉnh

Thay toàn bộ nội dung file `backend/src/main/java/vn/vcraft/backend/repository/AccountRepository.java`:

```java
package vn.vcraft.backend.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.model.Actor;

@Repository
public class AccountRepository {
    private final JdbcTemplate db;

    public AccountRepository(JdbcTemplate db) { this.db = db; }

    // ----------------------------------------------------------------
    // Giữ nguyên — đang được dùng
    // ----------------------------------------------------------------

    public record Credentials(long id, String passwordHash, String role) {}

    public Optional<Credentials> findActiveCredentials(String username) {
        return db.query("""
            SELECT a.id, a.password_hash, a.role
            FROM accounts a LEFT JOIN workshops w ON w.id = a.workshop_id
            WHERE a.username = ? AND (w.id IS NULL OR (w.approved AND w.locked_at IS NULL))
            """,
            (rs, row) -> new Credentials(rs.getLong("id"), rs.getString("password_hash"),
                rs.getString("role")),
            username).stream().findFirst();
        // ↑ Thêm w.locked_at IS NULL: hộ bị khóa không đăng nhập được
    }

    /**
     * Tìm phiên hợp lệ.
     * Thêm kiểm tra w.locked_at IS NULL: token của hộ bị khóa bị từ chối ngay.
     */
    public Optional<Actor> findActiveSession(String tokenHash) {
        return db.query("""
            SELECT a.id, a.role, a.workshop_id FROM accounts a
            JOIN account_sessions s ON s.account_id = a.id
            LEFT JOIN workshops w ON w.id = a.workshop_id
            WHERE s.token_hash = ? AND s.expires_at > now()
              AND (a.role != 'ARTISAN' OR (w.approved AND w.locked_at IS NULL))
            """,
            (rs, row) -> new Actor(rs.getLong("id"), rs.getString("role"),
                rs.getObject("workshop_id", Long.class)),
            tokenHash).stream().findFirst();
    }

    public void createSession(long accountId, String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE expires_at <= now()");
        db.update("""
            INSERT INTO account_sessions(token_hash, account_id, expires_at)
            VALUES (?, ?, now() + interval '168 hours')
            """, tokenHash, accountId);
        // 168h = 7 ngày (đổi từ 8h cũ)
    }

    public void revokeSession(String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE token_hash = ?", tokenHash);
    }

    public void addArtisan(String username, String passwordHash, long workshopId) {
        db.update("""
            INSERT INTO accounts(username, password_hash, role, workshop_id)
            VALUES (?, ?, 'ARTISAN', ?)
            """, username, passwordHash, workshopId);
    }

    public void seedAccount(String username, String passwordHash, String role) {
        db.update("""
            INSERT INTO accounts(username, password_hash, role)
            VALUES (?, ?, ?) ON CONFLICT(username) DO NOTHING
            """, username, passwordHash, role);
    }

    public void seedArtisan(String slug, String passwordHash) {
        db.update("""
            INSERT INTO accounts(username, password_hash, role, workshop_id)
            SELECT ?, ?, 'ARTISAN', id FROM workshops WHERE slug = ?
            ON CONFLICT DO NOTHING
            """, slug, passwordHash, slug);
    }

    // ----------------------------------------------------------------
    // Mới — M1 Auth Buyer
    // ----------------------------------------------------------------

    public long createBuyer(String username, String passwordHash,
                            String displayName, String email, String phone) {
        Long id = db.queryForObject("""
            INSERT INTO accounts(username, password_hash, role, display_name, email, phone)
            VALUES (?, ?, 'BUYER', ?, ?, ?) RETURNING id
            """, Long.class, username, passwordHash, displayName, email, phone);
        if (id == null) throw new IllegalStateException("Không tạo được tài khoản.");
        return id;
    }

    public boolean existsByUsername(String username) {
        var c = db.queryForObject(
            "SELECT COUNT(*) FROM accounts WHERE username = ?", Integer.class, username);
        return c != null && c > 0;
    }

    public boolean existsByEmail(String email) {
        var c = db.queryForObject(
            "SELECT COUNT(*) FROM accounts WHERE email = ?", Integer.class, email);
        return c != null && c > 0;
    }

    public record AccountEmailInfo(long id, String email, boolean emailVerified) {}

    public Optional<AccountEmailInfo> findEmailInfo(String username) {
        return db.query("""
            SELECT id, email, (email_verified_at IS NOT NULL) AS verified
            FROM accounts WHERE username = ?
            """,
            (rs, r) -> new AccountEmailInfo(rs.getLong("id"), rs.getString("email"),
                rs.getBoolean("verified")),
            username).stream().findFirst();
    }

    public void markEmailVerified(long accountId) {
        db.update("UPDATE accounts SET email_verified_at = now() WHERE id = ?", accountId);
    }

    public boolean isEmailVerified(long accountId) {
        var c = db.queryForObject("""
            SELECT COUNT(*) FROM accounts
            WHERE id = ? AND email_verified_at IS NOT NULL
            """, Integer.class, accountId);
        return c != null && c > 0;
    }

    // ----------------------------------------------------------------
    // Mới — Admin
    // ----------------------------------------------------------------

    public void revokeAllSessionsForWorkshop(long workshopId) {
        db.update("""
            DELETE FROM account_sessions
            WHERE account_id IN (SELECT id FROM accounts WHERE workshop_id = ?)
            """, workshopId);
    }

    public long countBuyers() {
        var c = db.queryForObject(
            "SELECT COUNT(*) FROM accounts WHERE role = 'BUYER'", Long.class);
        return c != null ? c : 0L;
    }
}
```

---

## 4. Frontend — Tách file và thêm Auth (không phá route cũ)

### 4.1. Tạo thư mục mới

```bash
mkdir -p frontend/src/api
mkdir -p frontend/src/auth
mkdir -p frontend/src/pages/auth
mkdir -p frontend/src/pages/buyer
mkdir -p frontend/src/pages/artisan
mkdir -p frontend/src/pages/admin
```

### 4.2. frontend/src/api/client.js

```javascript
// Fetch wrapper — tất cả API call đi qua đây
const BASE = '/api';

export function getToken() {
  return sessionStorage.getItem('vcraft_token');
}

export function setToken(token) {
  if (token) sessionStorage.setItem('vcraft_token', token);
  else sessionStorage.removeItem('vcraft_token');
}

async function request(method, path, body, isMultipart = false) {
  const token = getToken();
  const headers = { 'Accept': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!isMultipart && body) headers['Content-Type'] = 'application/json';

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: isMultipart ? body
        : (body != null ? JSON.stringify(body) : undefined),
  });

  if (res.status === 204) return null;

  let data;
  try { data = await res.json(); } catch { data = {}; }

  if (!res.ok) {
    const err = new Error(data.message || `HTTP ${res.status}`);
    err.status  = res.status;
    err.code    = data.code;
    err.fieldErrors = data.fieldErrors ?? [];
    throw err;
  }
  return data;
}

export const api = {
  get:    path        => request('GET',    path),
  post:   (path, body) => request('POST',   path, body),
  put:    (path, body) => request('PUT',    path, body),
  patch:  (path, body) => request('PATCH',  path, body),
  del:    path        => request('DELETE', path),
  upload: (path, fd)  => request('POST',   path, fd, true),
};
```

### 4.3. frontend/src/auth/AuthContext.jsx

```jsx
import React, { createContext, useContext, useEffect, useState } from 'react';
import { api, setToken } from '../api/client.js';

const Ctx = createContext(null);

export function AuthProvider({ children }) {
  // undefined = chưa kiểm tra, null = chưa đăng nhập, object = đã đăng nhập
  const [user, setUser] = useState(undefined);

  useEffect(() => {
    const token = sessionStorage.getItem('vcraft_token');
    if (!token) { setUser(null); return; }
    api.get('/me')
      .then(me => setUser(me))
      .catch(() => { setToken(null); setUser(null); });
  }, []);

  async function login(username, password) {
    const res = await api.post('/login', { username, password });
    setToken(res.token);
    // Lấy thông tin đầy đủ
    const me = await api.get('/me');
    setUser(me);
    return me;
  }

  async function logout() {
    try { await api.post('/logout'); } catch { /* bỏ qua */ }
    setToken(null);
    setUser(null);
  }

  return (
    <Ctx.Provider value={{ user, login, logout }}>
      {user === undefined
        ? <p className="notice" style={{textAlign:'center',marginTop:'4rem'}}>Đang tải…</p>
        : children}
    </Ctx.Provider>
  );
}

export const useAuth = () => useContext(Ctx);
```

### 4.4. frontend/src/auth/RequireAuth.jsx

```jsx
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

export function RequireAuth({ role, children }) {
  const { user } = useAuth();
  const location  = useLocation();

  if (!user) return <Navigate to="/dang-nhap" state={{ from: location }} replace />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return children;
}
```

### 4.5. frontend/src/pages/auth/Login.jsx

```jsx
import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate   = useNavigate();
  const location   = useLocation();
  const from       = location.state?.from?.pathname ?? '/';

  const [form, setForm]   = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy]   = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const me = await login(form.username, form.password);
      // Redirect theo role
      if (me.role === 'ADMIN')    navigate('/admin', { replace: true });
      else if (me.role === 'ARTISAN') navigate('/quan-ly/san-pham', { replace: true });
      else navigate(from, { replace: true });
    } catch (err) {
      setError(err.message ?? 'Đăng nhập thất bại.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section style={{ maxWidth: 400, margin: '3rem auto' }}>
      <h1 style={{ fontSize: '1.8rem' }}>Đăng nhập</h1>
      {error && <p className="notice" role="alert">{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <label>
          Tên đăng nhập
          <input type="text" required autoFocus
                 value={form.username}
                 onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                 style={{ display:'block', width:'100%', marginTop:4, padding:'8px 12px',
                          border:'1px solid #dfd8ca', borderRadius:8, font:'inherit' }} />
        </label>
        <label>
          Mật khẩu
          <input type="password" required
                 value={form.password}
                 onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                 style={{ display:'block', width:'100%', marginTop:4, padding:'8px 12px',
                          border:'1px solid #dfd8ca', borderRadius:8, font:'inherit' }} />
        </label>
        <button className="button" type="submit" disabled={busy}>
          {busy ? 'Đang đăng nhập…' : 'Đăng nhập'}
        </button>
      </form>
      <p style={{ marginTop: 16, fontSize: '.9rem' }}>
        Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
      </p>
    </section>
  );
}
```

### 4.6. frontend/src/pages/auth/Register.jsx

```jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../../api/client.js';

const field = (label, type, key, form, setForm, hint) => (
  <label style={{ display:'flex', flexDirection:'column', gap:4 }}>
    <span>{label}</span>
    <input type={type} required value={form[key]}
           onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
           style={{ padding:'8px 12px', border:'1px solid #dfd8ca',
                    borderRadius:8, font:'inherit' }} />
    {hint && <small style={{ color:'#666' }}>{hint}</small>}
  </label>
);

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username:'', password:'', displayName:'', email:'', phone:''
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError]             = useState('');
  const [busy, setBusy]               = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setBusy(true); setError(''); setFieldErrors({});
    try {
      await api.post('/register', form);
      navigate('/xac-thuc-email', { state: { username: form.username } });
    } catch (err) {
      if (err.fieldErrors?.length) {
        const map = {};
        err.fieldErrors.forEach(fe => { map[fe.field] = fe.message; });
        setFieldErrors(map);
      } else {
        setError(err.message ?? 'Đăng ký thất bại.');
      }
    } finally {
      setBusy(false);
    }
  }

  const fe = (key) => fieldErrors[key]
    ? <small style={{ color:'#c00' }}>{fieldErrors[key]}</small>
    : null;

  return (
    <section style={{ maxWidth: 440, margin: '3rem auto' }}>
      <h1 style={{ fontSize: '1.8rem' }}>Đăng ký tài khoản</h1>
      {error && <p className="notice" role="alert">{error}</p>}
      <form onSubmit={handleSubmit} style={{ display:'flex', flexDirection:'column', gap:14 }}>
        {field('Tên đăng nhập', 'text', 'username', form, setForm, 'Chỉ dùng chữ thường, số và dấu gạch ngang (3–60 ký tự)')}
        {fe('username')}
        {field('Mật khẩu', 'password', 'password', form, setForm, 'Tối thiểu 8 ký tự')}
        {fe('password')}
        {field('Họ và tên', 'text', 'displayName', form, setForm)}
        {fe('displayName')}
        {field('Email', 'email', 'email', form, setForm, 'Dùng để nhận mã xác thực')}
        {fe('email')}
        {field('Số điện thoại', 'tel', 'phone', form, setForm)}
        {fe('phone')}
        <button className="button" type="submit" disabled={busy}>
          {busy ? 'Đang đăng ký…' : 'Tạo tài khoản'}
        </button>
      </form>
      <p style={{ marginTop:16, fontSize:'.9rem' }}>
        Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link>
      </p>
    </section>
  );
}
```

### 4.7. frontend/src/pages/auth/VerifyEmail.jsx

```jsx
import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { api } from '../../api/client.js';

export default function VerifyEmail() {
  const location = useLocation();
  const navigate = useNavigate();
  const username = location.state?.username ?? '';

  const [otp, setOtp]     = useState('');
  const [msg, setMsg]     = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy]   = useState(false);

  async function handleVerify(e) {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      await api.post('/register/verify-email', { username, otp });
      navigate('/dang-nhap', {
        state: { notice: 'Email đã xác thực. Vui lòng đăng nhập.' }
      });
    } catch (err) {
      setError(err.message ?? 'Mã xác thực không đúng hoặc đã hết hạn.');
    } finally {
      setBusy(false);
    }
  }

  async function handleResend() {
    setBusy(true); setError(''); setMsg('');
    try {
      await api.post('/register/resend-otp', { username });
      setMsg('Đã gửi lại mã xác thực. Kiểm tra hộp thư.');
    } catch (err) {
      setError(err.message ?? 'Không gửi được. Thử lại sau.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section style={{ maxWidth: 400, margin: '3rem auto' }}>
      <h1 style={{ fontSize: '1.8rem' }}>Xác thực email</h1>
      <p>Nhập mã 6 chữ số đã gửi đến email của bạn.</p>
      {error && <p className="notice" role="alert" style={{ color:'#c00' }}>{error}</p>}
      {msg   && <p className="notice" role="status">{msg}</p>}
      <form onSubmit={handleVerify} style={{ display:'flex', flexDirection:'column', gap:14 }}>
        <input type="text" inputMode="numeric" pattern="[0-9]{6}" maxLength={6}
               placeholder="000000" required value={otp}
               onChange={e => setOtp(e.target.value.replace(/\D/g, ''))}
               style={{ padding:'12px', fontSize:'1.5rem', textAlign:'center',
                        border:'1px solid #dfd8ca', borderRadius:8,
                        letterSpacing:'0.4em' }} />
        <button className="button" type="submit" disabled={busy || otp.length !== 6}>
          {busy ? 'Đang xác thực…' : 'Xác thực'}
        </button>
      </form>
      <button type="button" onClick={handleResend} disabled={busy}
              style={{ marginTop:12, background:'none', border:'none',
                       cursor:'pointer', textDecoration:'underline', font:'inherit' }}>
        Gửi lại mã
      </button>
    </section>
  );
}
```

### 4.8. frontend/src/main.jsx — Phiên bản module hóa

Thay toàn bộ nội dung file `frontend/src/main.jsx`. **Giữ nguyên các route `/`, `/san-pham`, `/san-pham/:id` cũ — các component `Home`, `Catalog`, `Detail` được giữ lại.**

```jsx
import React, { useEffect, useState, lazy, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import {
  BrowserRouter, Link, NavLink, Route, Routes,
  useParams, useNavigate
} from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthContext.jsx';
import { RequireAuth } from './auth/RequireAuth.jsx';
import { api, setToken } from './api/client.js';
import './styles.css';

// ----------------------------------------------------------------
// Lazy pages — tạo file dần, không cần có ngay
// ----------------------------------------------------------------
const Login       = lazy(() => import('./pages/auth/Login.jsx'));
const Register    = lazy(() => import('./pages/auth/Register.jsx'));
const VerifyEmail = lazy(() => import('./pages/auth/VerifyEmail.jsx'));

// Các page sau sẽ tạo trong M2:
// const WorkshopList   = lazy(() => import('./pages/WorkshopList.jsx'));
// const WorkshopDetail = lazy(() => import('./pages/WorkshopDetail.jsx'));
// const Map            = lazy(() => import('./pages/Map.jsx'));
// const BatchDetail    = lazy(() => import('./pages/BatchDetail.jsx'));
// const QuoteInbox     = lazy(() => import('./pages/artisan/QuoteInbox.jsx'));
// v.v.

// ----------------------------------------------------------------
// Giữ nguyên các component cũ (đang hoạt động)
// ----------------------------------------------------------------
function useCatalog(path) {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult]   = useState(null);
  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const response = await fetch(path, {
          headers: { Accept: 'application/json' },
          signal: controller.signal,
        });
        if (response.status === 404) { setResult({ path, attempt, missing: true }); return; }
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
    retry:   () => setAttempt(v => v + 1),
  };
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
  if (!product.imageUrl || failed)
    return <div className="image-placeholder">Chưa có ảnh minh họa</div>;
  return <img src={product.imageUrl} alt={product.name} width="640" height="480"
              onError={() => setFailed(true)} />;
}

function Home() {
  return (
    <section className="hero">
      <div className="hero-copy">
        <p className="eyebrow">BÁT TRÀNG · TRIỂN LÃM SỐ</p>
        <h1>Gốm làng nghề</h1>
        <p>Khám phá những dáng gốm, màu men và câu chuyện từ các xưởng thủ công Việt.</p>
        <Link className="button" to="/san-pham">
          Khám phá danh mục <span aria-hidden="true">→</span>
        </Link>
      </div>
      <img src="/images/binh-men-lam.svg" alt="Minh họa bình gốm men lam"
           width="640" height="480" />
    </section>
  );
}

function Catalog() {
  const [page, setPage] = useState(0);
  const { current, retry } = useCatalog(`/api/products/page?page=${page}&size=12`);
  return (
    <section>
      <p className="eyebrow">BỘ SƯU TẬP</p>
      <h1>Danh mục sản phẩm</h1>
      {!current?.data ? <ResultState current={current} retry={retry} /> :
       current.data.items.length === 0
         ? <p className="notice" role="status">Danh mục hiện chưa có sản phẩm.</p>
         : <ul className="grid">
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
          <button type="button" disabled={current.data.first}
                  onClick={() => setPage(p => p - 1)}>Trang trước</button>
          <span aria-live="polite">
            Trang {page + 1} / {current.data.totalPages} · {current.data.totalElements} sản phẩm
          </span>
          <button type="button" disabled={current.data.last}
                  onClick={() => setPage(p => p + 1)}>Trang sau</button>
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

// ----------------------------------------------------------------
// Header — thêm menu đăng nhập/đăng xuất
// ----------------------------------------------------------------
function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/');
  }

  return (
    <header>
      <div className="shell nav">
        <Link className="brand" to="/">vCraft<span>.</span></Link>
        <nav aria-label="Điều hướng chính">
          <NavLink to="/" end>Trang chủ</NavLink>
          <NavLink to="/san-pham">Danh mục</NavLink>
          {/* Thêm sau khi M2A xong: */}
          {/* <NavLink to="/ho-gia-dinh">Xưởng</NavLink> */}
          {/* <NavLink to="/ban-do">Bản đồ</NavLink>  */}

          {/* Auth links */}
          {!user && <NavLink to="/dang-nhap">Đăng nhập</NavLink>}
          {user?.role === 'ARTISAN' && (
            <NavLink to="/quan-ly/san-pham">Quản lý</NavLink>
          )}
          {user?.role === 'BUYER' && (
            <NavLink to="/yeu-cau">Yêu cầu</NavLink>
          )}
          {user?.role === 'ADMIN' && (
            <NavLink to="/admin">Quản trị</NavLink>
          )}
          {user && (
            <button type="button" onClick={handleLogout}
                    style={{ background:'none', border:'none', cursor:'pointer',
                             font:'inherit', padding:0 }}>
              Đăng xuất
            </button>
          )}
        </nav>
      </div>
    </header>
  );
}

// ----------------------------------------------------------------
// App
// ----------------------------------------------------------------
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <main className="shell">
          <Suspense fallback={<p className="notice">Đang tải…</p>}>
            <Routes>
              {/* Route cũ — giữ nguyên */}
              <Route path="/"              element={<Home />} />
              <Route path="/san-pham"     element={<Catalog />} />
              <Route path="/san-pham/:id" element={<Detail />} />

              {/* Auth */}
              <Route path="/dang-nhap"     element={<Login />} />
              <Route path="/dang-ky"       element={<Register />} />
              <Route path="/xac-thuc-email" element={<VerifyEmail />} />

              {/* Route M2 — bỏ comment khi tạo xong file */}
              {/*
              <Route path="/ho-gia-dinh"         element={<WorkshopList />} />
              <Route path="/ho-gia-dinh/:slug"   element={<WorkshopDetail />} />
              <Route path="/ban-do"              element={<Map />} />
              <Route path="/batches/:publicCode" element={<BatchDetail />} />
              <Route path="/tin-tuc"             element={<NewsList />} />

              <Route path="/ho-so-xuong" element={
                <RequireAuth role="ARTISAN"><WorkshopProfile /></RequireAuth>} />
              <Route path="/quan-ly/san-pham" element={
                <RequireAuth role="ARTISAN"><ProductManage /></RequireAuth>} />
              <Route path="/quan-ly/lo" element={
                <RequireAuth role="ARTISAN"><BatchManage /></RequireAuth>} />
              <Route path="/quan-ly/bao-gia" element={
                <RequireAuth role="ARTISAN"><QuoteInbox /></RequireAuth>} />
              <Route path="/yeu-cau" element={
                <RequireAuth role="BUYER"><QuoteList /></RequireAuth>} />
              <Route path="/yeu-cau/:id" element={
                <RequireAuth role="BUYER"><QuoteDetail /></RequireAuth>} />
              <Route path="/admin" element={
                <RequireAuth role="ADMIN"><AdminDashboard /></RequireAuth>} />
              */}

              <Route path="*" element={
                <section>
                  <h1>Không tìm thấy trang</h1>
                  <Link to="/">Về trang chủ</Link>
                </section>} />
            </Routes>
          </Suspense>
        </main>
        <footer className="shell">
          vCraft · INT3306 Fall 2026 · Làng nghề Bát Tràng
        </footer>
      </BrowserRouter>
    </AuthProvider>
  );
}

createRoot(document.getElementById('root')).render(<App />);
```

---

## 5. Kiểm tra sau khi copy

```bash
# Backend: chạy migration + khởi động
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Phải thấy trong log:
# Flyway: Successfully applied 2 migrations (V5, V6)
# Started VCraftApplication

# Test nhanh:
curl -s http://localhost:8080/api/health | python3 -m json.tool

# Frontend: chạy dev server
cd frontend
npm install
npm run dev

# Mở http://localhost:5173
# Phải thấy: Header có "Đăng nhập" link
# /san-pham vẫn hoạt động bình thường
# /dang-nhap hiển thị form đăng nhập
# Đăng nhập với admin/VcraftDemo17! → redirect /admin
# Đăng nhập với artisan_men_lam/VcraftDemo17! → redirect /quan-ly/san-pham
# (các route này chưa có page, sẽ hiện "Không tìm thấy trang" — đúng)
```

---

## 6. Lỗi thường gặp và cách xử lý

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| `Schema-validation: missing column [description] in table [workshops]` | V5 chưa chạy | Kiểm tra `flyway_schema_history`; đảm bảo V5 file có nội dung đúng |
| `Schema-validation: missing column [reference_price] in table [products]` | Product entity thêm field nhưng V5 chưa migrate | Chạy migration trước khi restart |
| `No qualifying bean of type 'OtpService'` | Chưa tạo file OtpService.java | Tạo file hoặc tạm comment chỗ inject |
| `Could not resolve placeholder 'cors.allowed-origins'` | Thiếu dòng trong application-local.properties | Thêm `cors.allowed-origins=http://localhost:5173` |
| `Failed to load module script: main.jsx` | Vite chưa cài `@vitejs/plugin-react` | Thêm plugin (xem ghi chú bên dưới) |

**Ghi chú Vite + React:** File `vite.config.js` hiện tại cần kiểm tra có `@vitejs/plugin-react` không. Nếu chưa:

```bash
npm install -D @vitejs/plugin-react
```

```javascript
// frontend/vite.config.js — đảm bảo có plugin
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { proxy: { '/api': 'http://localhost:8080' } },
});
```
