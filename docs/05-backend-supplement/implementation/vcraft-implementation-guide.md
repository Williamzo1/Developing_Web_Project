# vCraft Implementation Guide
# Hướng dẫn triển khai chi tiết — Code sẵn sàng copy vào repo

> Tài liệu này bổ sung cho Blueprint và Phase C, tập trung vào những phần
> chưa có code mẫu: Repository SQL, Seed data V5, cấu trúc Frontend.

---

## PHẦN 1 — REPOSITORY IMPLEMENTATIONS

### 1.1. WorkshopRepository.java — Toàn bộ code

```java
package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkshopRepository {
    private final JdbcTemplate db;

    public WorkshopRepository(JdbcTemplate db) { this.db = db; }

    // ----------------------------------------------------------------
    // ADMIN — đã có, giữ nguyên
    // ----------------------------------------------------------------

    public List<WorkshopAdminRow> list() {
        return db.query("""
            SELECT w.id, w.name, w.slug, w.approved, w.locked_at, a.username
            FROM workshops w LEFT JOIN accounts a ON a.workshop_id = w.id
            ORDER BY w.id
            """,
            (rs, r) -> new WorkshopAdminRow(
                rs.getLong("id"), rs.getString("name"), rs.getString("slug"),
                rs.getBoolean("approved"), rs.getObject("locked_at", Instant.class),
                rs.getString("username")));
    }

    public WorkshopResponse createPending(String slug, String name) {
        return db.queryForObject("""
            INSERT INTO workshops(slug, name, approved) VALUES (?, ?, false)
            RETURNING id, name, approved
            """,
            (rs, r) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                null, rs.getBoolean("approved"), null),
            slug, name);
    }

    public Optional<WorkshopResponse> approve(long id) {
        return db.query("""
            UPDATE workshops SET approved = true, approved_at = now()
            WHERE id = ? RETURNING id, name, approved
            """,
            (rs, r) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                null, rs.getBoolean("approved"), null),
            id).stream().findFirst();
    }

    // lock/unlock — dùng trong AdminService
    public void lock(long id, String reason) {
        db.update("""
            UPDATE workshops SET locked_at = now(), locked_reason = ?
            WHERE id = ?
            """, reason, id);
    }

    public void unlock(long id) {
        db.update("UPDATE workshops SET locked_at = NULL, locked_reason = NULL WHERE id = ?", id);
    }

    // ----------------------------------------------------------------
    // PUBLIC — Danh sách và hồ sơ
    // ----------------------------------------------------------------

    /**
     * Danh sách hộ công khai (approved, chưa bị khóa).
     * Trả WorkshopSummaryResponse — chỉ dùng thumbUrl cho ảnh bìa.
     */
    public List<WorkshopSummaryResponse> listPublic(int limit, int offset, String baseUrl) {
        return db.query("""
            SELECT
                w.id, w.slug, w.name, w.description,
                w.accepts_visitors,
                m.path_thumb AS cover_thumb,
                m.width AS cover_w, m.height AS cover_h
            FROM workshops w
            LEFT JOIN media m ON m.id = w.cover_media_id
            WHERE w.approved = TRUE AND w.locked_at IS NULL
            ORDER BY w.featured DESC, w.id
            LIMIT ? OFFSET ?
            """,
            (rs, r) -> new WorkshopSummaryResponse(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("name"),
                rs.getString("description"),
                toMediaThumb(rs.getString("cover_thumb"), rs.getInt("cover_w"),
                             rs.getInt("cover_h"), baseUrl),
                rs.getBoolean("accepts_visitors"),
                null),  // openNow tính riêng nếu cần; để null cho list (tiết kiệm query)
            limit, offset);
    }

    public long countPublic() {
        var count = db.queryForObject("""
            SELECT COUNT(*) FROM workshops
            WHERE approved = TRUE AND locked_at IS NULL
            """, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Chi tiết hộ theo slug.
     * Trả đủ thông tin để render hồ sơ công khai.
     */
    public Optional<WorkshopDetailRow> findBySlug(String slug) {
        return db.query("""
            SELECT
                w.id, w.slug, w.name, w.description, w.family_history,
                w.address, w.latitude, w.longitude,
                w.contact_email, w.phone, w.facebook_url,
                w.accepts_visitors, w.created_at, w.updated_at,
                m.path_thumb, m.path_medium, m.path_large,
                m.width, m.height
            FROM workshops w
            LEFT JOIN media m ON m.id = w.cover_media_id
            WHERE w.slug = ? AND w.approved = TRUE AND w.locked_at IS NULL
            """,
            (rs, r) -> new WorkshopDetailRow(
                rs.getLong("id"), rs.getString("slug"), rs.getString("name"),
                rs.getString("description"), rs.getString("family_history"),
                rs.getString("address"),
                rs.getObject("latitude", BigDecimal.class),
                rs.getObject("longitude", BigDecimal.class),
                rs.getString("contact_email"), rs.getString("phone"),
                rs.getString("facebook_url"),
                rs.getBoolean("accepts_visitors"),
                rs.getObject("created_at", Instant.class),
                rs.getObject("updated_at", Instant.class),
                rs.getString("path_thumb"), rs.getString("path_medium"),
                rs.getString("path_large"), rs.getInt("width"), rs.getInt("height")),
            slug).stream().findFirst();
    }

    /** Hồ sơ của Artisan đang đăng nhập (kể cả chưa approved). */
    public Optional<WorkshopDetailRow> findByWorkshopId(long workshopId) {
        return db.query("""
            SELECT w.id, w.slug, w.name, w.description, w.family_history,
                   w.address, w.latitude, w.longitude,
                   w.contact_email, w.phone, w.facebook_url,
                   w.accepts_visitors, w.created_at, w.updated_at,
                   m.path_thumb, m.path_medium, m.path_large,
                   m.width, m.height
            FROM workshops w
            LEFT JOIN media m ON m.id = w.cover_media_id
            WHERE w.id = ?
            """,
            (rs, r) -> new WorkshopDetailRow(
                rs.getLong("id"), rs.getString("slug"), rs.getString("name"),
                rs.getString("description"), rs.getString("family_history"),
                rs.getString("address"),
                rs.getObject("latitude", BigDecimal.class),
                rs.getObject("longitude", BigDecimal.class),
                rs.getString("contact_email"), rs.getString("phone"),
                rs.getString("facebook_url"),
                rs.getBoolean("accepts_visitors"),
                rs.getObject("created_at", Instant.class),
                rs.getObject("updated_at", Instant.class),
                rs.getString("path_thumb"), rs.getString("path_medium"),
                rs.getString("path_large"), rs.getInt("width"), rs.getInt("height")),
            workshopId).stream().findFirst();
    }

    /** Ghim bản đồ — tất cả hộ có tọa độ. */
    public List<MapPinResponse> mapPins(String baseUrl) {
        return db.query("""
            SELECT
                w.id, w.slug, w.name, w.address,
                w.latitude, w.longitude, w.accepts_visitors,
                m.path_thumb, m.width, m.height
            FROM workshops w
            LEFT JOIN media m ON m.id = w.cover_media_id
            WHERE w.approved = TRUE AND w.locked_at IS NULL
              AND w.latitude IS NOT NULL AND w.longitude IS NOT NULL
            ORDER BY w.id
            """,
            (rs, r) -> new MapPinResponse(
                rs.getLong("id"), rs.getString("slug"), rs.getString("name"),
                rs.getObject("latitude", BigDecimal.class),
                rs.getObject("longitude", BigDecimal.class),
                toMediaThumb(rs.getString("path_thumb"), rs.getInt("width"),
                             rs.getInt("height"), baseUrl),
                rs.getBoolean("accepts_visitors"),
                null,  // openNow — tính trong WorkshopService
                rs.getString("address")));
    }

    // ----------------------------------------------------------------
    // ARTISAN — Cập nhật hồ sơ
    // ----------------------------------------------------------------

    public void updateProfile(long workshopId,
                              String name, String description, String familyHistory,
                              String address, BigDecimal latitude, BigDecimal longitude,
                              String contactEmail, String phone, String facebookUrl,
                              Boolean acceptsVisitors, Long coverMediaId) {
        db.update("""
            UPDATE workshops SET
                name = ?, description = ?, family_history = ?,
                address = ?, latitude = ?, longitude = ?,
                contact_email = ?, phone = ?, facebook_url = ?,
                accepts_visitors = ?, cover_media_id = ?,
                updated_at = now()
            WHERE id = ?
            """,
            name, description, familyHistory,
            address, latitude, longitude,
            contactEmail, phone, facebookUrl,
            acceptsVisitors, coverMediaId,
            workshopId);
    }

    // ----------------------------------------------------------------
    // CERTIFICATES
    // ----------------------------------------------------------------

    public List<CertificateResponse> findCertificates(long workshopId) {
        return db.query("""
            SELECT id, name, issuer, issued_on, created_at
            FROM certificates WHERE workshop_id = ? ORDER BY id
            """,
            (rs, r) -> new CertificateResponse(
                rs.getLong("id"), rs.getString("name"), rs.getString("issuer"),
                rs.getObject("issued_on", java.time.LocalDate.class),
                rs.getObject("created_at", Instant.class)),
            workshopId);
    }

    public CertificateResponse addCertificate(long workshopId, String name,
                                               String issuer, java.time.LocalDate issuedOn) {
        return db.queryForObject("""
            INSERT INTO certificates(workshop_id, name, issuer, issued_on)
            VALUES (?, ?, ?, ?) RETURNING id, name, issuer, issued_on, created_at
            """,
            (rs, r) -> new CertificateResponse(
                rs.getLong("id"), rs.getString("name"), rs.getString("issuer"),
                rs.getObject("issued_on", java.time.LocalDate.class),
                rs.getObject("created_at", Instant.class)),
            workshopId, name, issuer, issuedOn);
    }

    public boolean deleteCertificate(long workshopId, long certificateId) {
        int rows = db.update("""
            DELETE FROM certificates WHERE id = ? AND workshop_id = ?
            """, certificateId, workshopId);
        return rows > 0;
    }

    // ----------------------------------------------------------------
    // FEATURED (Admin)
    // ----------------------------------------------------------------

    public void clearAllFeatured() {
        db.update("UPDATE workshops SET featured = FALSE WHERE featured = TRUE");
    }

    public void setFeatured(long id, boolean featured) {
        db.update("UPDATE workshops SET featured = ? WHERE id = ?", featured, id);
    }

    // ----------------------------------------------------------------
    // STATS (Admin)
    // ----------------------------------------------------------------

    public long countAll() {
        var count = db.queryForObject("SELECT COUNT(*) FROM workshops", Long.class);
        return count != null ? count : 0L;
    }

    public long countApproved() {
        var count = db.queryForObject(
            "SELECT COUNT(*) FROM workshops WHERE approved = TRUE AND locked_at IS NULL",
            Long.class);
        return count != null ? count : 0L;
    }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    private static MediaResponse toMediaThumb(String path, int w, int h, String baseUrl) {
        if (path == null) return null;
        return new MediaResponse(0L,
            baseUrl + "/media/" + path, null, null, w, h);
    }

    // DTO nội bộ — không expose ra ngoài package nếu chỉ dùng trong repo
    public record WorkshopDetailRow(
        long id, String slug, String name,
        String description, String familyHistory,
        String address, BigDecimal latitude, BigDecimal longitude,
        String contactEmail, String phone, String facebookUrl,
        boolean acceptsVisitors, Instant createdAt, Instant updatedAt,
        String pathThumb, String pathMedium, String pathLarge,
        int width, int height) {}

    public record WorkshopAdminRow(
        long id, String name, String slug, boolean approved,
        Instant lockedAt, String username) {}
}
```

---

### 1.2. WorkshopHourRepository.java — Toàn bộ code

```java
package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.WorkshopHourInput;
import vn.vcraft.backend.dto.WorkshopHourResponse;

import java.sql.Time;
import java.time.*;
import java.util.List;

@Repository
public class WorkshopHourRepository {
    private final JdbcTemplate db;

    public WorkshopHourRepository(JdbcTemplate db) { this.db = db; }

    /** Thay toàn bộ giờ mở cửa của xưởng. */
    public void replaceHours(long workshopId, List<WorkshopHourInput> hours) {
        db.update("DELETE FROM workshop_hours WHERE workshop_id = ?", workshopId);
        for (var h : hours) {
            db.update("""
                INSERT INTO workshop_hours(workshop_id, day_of_week, open_time, close_time)
                VALUES (?, ?, ?::time, ?::time)
                ON CONFLICT (workshop_id, day_of_week) DO UPDATE
                    SET open_time = EXCLUDED.open_time, close_time = EXCLUDED.close_time
                """,
                workshopId, h.dayOfWeek(), h.openTime(), h.closeTime());
        }
    }

    public List<WorkshopHourResponse> findByWorkshop(long workshopId) {
        return db.query("""
            SELECT day_of_week, open_time, close_time
            FROM workshop_hours WHERE workshop_id = ? ORDER BY day_of_week
            """,
            (rs, r) -> new WorkshopHourResponse(
                rs.getInt("day_of_week"),
                rs.getString("open_time"),
                rs.getString("close_time")),
            workshopId);
    }

    /**
     * Tính trạng thái mở cửa hiện tại (UTC+7 — Hà Nội).
     * Trả null nếu hộ chưa khai báo giờ mở cửa.
     * Trả false nếu hôm nay nằm trong workshop_closures.
     */
    public Boolean isOpenNow(long workshopId) {
        ZonedDateTime nowHanoi = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = nowHanoi.toLocalDate();
        LocalTime nowTime = nowHanoi.toLocalTime();
        int dayOfWeek = nowHanoi.getDayOfWeek().getValue() % 7; // 0=CN, 1=T2…6=T7

        // Kiểm tra ngày đóng cửa đột xuất
        var closed = db.queryForObject("""
            SELECT COUNT(*) FROM workshop_closures
            WHERE workshop_id = ? AND closed_on = ?
            """, Integer.class, workshopId, today);
        if (closed != null && closed > 0) return false;

        // Kiểm tra giờ hôm nay
        return db.query("""
            SELECT open_time, close_time FROM workshop_hours
            WHERE workshop_id = ? AND day_of_week = ?
            """,
            (rs, r) -> {
                Time open = rs.getTime("open_time");
                Time close = rs.getTime("close_time");
                LocalTime openTime = open.toLocalTime();
                LocalTime closeTime = close.toLocalTime();
                return nowTime.isAfter(openTime) && nowTime.isBefore(closeTime);
            },
            workshopId, dayOfWeek).stream().findFirst().orElse(null);
        // null = không có khai báo giờ cho thứ này
    }

    public void addClosure(long workshopId, LocalDate closedOn, String reason) {
        db.update("""
            INSERT INTO workshop_closures(workshop_id, closed_on, reason)
            VALUES (?, ?, ?)
            ON CONFLICT (workshop_id, closed_on) DO UPDATE SET reason = EXCLUDED.reason
            """, workshopId, closedOn, reason);
    }

    public boolean deleteClosure(long workshopId, long closureId) {
        int rows = db.update("""
            DELETE FROM workshop_closures WHERE id = ? AND workshop_id = ?
            """, closureId, workshopId);
        return rows > 0;
    }
}
```

---

### 1.3. BatchRepository.java — Toàn bộ code

```java
package vn.vcraft.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.Batch;

import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    /** Tìm theo public_code — dùng cho landing page và QR. */
    @Query("SELECT b FROM Batch b WHERE b.publicCode = :code")
    Optional<Batch> findByPublicCode(@Param("code") String code);

    boolean existsByPublicCode(String publicCode);

    /** Danh sách lô của xưởng (kể cả ẩn — artisan xem). */
    @Query("""
        SELECT b FROM Batch b WHERE b.workshopId = :workshopId
        ORDER BY b.createdAt DESC
        """)
    java.util.List<Batch> findByWorkshopId(@Param("workshopId") long workshopId);

    /** Danh sách lô công khai (phân trang, có filter). */
    @Query(value = """
        SELECT b FROM Batch b
        WHERE b.visibility = 'PUBLIC' AND b.hidden = FALSE
          AND (:workshopId IS NULL OR b.workshopId = :workshopId)
          AND (:productId IS NULL OR b.productId = :productId)
          AND (:kind IS NULL OR b.kind = :kind)
        ORDER BY b.featured DESC, b.createdAt DESC
        """,
        countQuery = """
        SELECT COUNT(b) FROM Batch b
        WHERE b.visibility = 'PUBLIC' AND b.hidden = FALSE
          AND (:workshopId IS NULL OR b.workshopId = :workshopId)
          AND (:productId IS NULL OR b.productId = :productId)
          AND (:kind IS NULL OR b.kind = :kind)
        """)
    Page<Batch> findPublicFiltered(
        @Param("workshopId") Long workshopId,
        @Param("productId") Long productId,
        @Param("kind") String kind,
        Pageable pageable);

    /** Lô nổi bật (trang chủ). */
    @Query("SELECT b FROM Batch b WHERE b.featured = TRUE AND b.hidden = FALSE AND b.visibility = 'PUBLIC'")
    java.util.List<Batch> findFeatured();

    @Modifying
    @Query("UPDATE Batch b SET b.hidden = TRUE WHERE b.id = :id")
    void hide(@Param("id") long id);

    @Modifying
    @Query("UPDATE Batch b SET b.featured = FALSE WHERE b.featured = TRUE")
    void clearAllFeatured();

    @Modifying
    @Query("UPDATE Batch b SET b.featured = :featured WHERE b.id = :id")
    void setFeatured(@Param("id") long id, @Param("featured") boolean featured);
}
```

---

### 1.4. BatchMilestoneRepository.java

```java
package vn.vcraft.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.BatchMilestone;
import java.util.List;

public interface BatchMilestoneRepository extends JpaRepository<BatchMilestone, Long> {

    @Query("""
        SELECT m FROM BatchMilestone m
        WHERE m.batchId = :batchId ORDER BY m.happenedOn, m.id
        """)
    List<BatchMilestone> findByBatchId(@Param("batchId") long batchId);

    @Modifying
    @Query("DELETE FROM BatchMilestone m WHERE m.batchId = :batchId")
    void deleteByBatchId(@Param("batchId") long batchId);
}
```

---

### 1.5. NewsRepository.java

```java
package vn.vcraft.backend.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.NewsPost;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<NewsPost, Long> {

    @Query(value = """
        SELECT n FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
          AND (:kind IS NULL OR n.kind = :kind)
        ORDER BY n.publishedAt DESC
        """,
        countQuery = """
        SELECT COUNT(n) FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
          AND (:kind IS NULL OR n.kind = :kind)
        """)
    Page<NewsPost> findPublished(@Param("kind") String kind, Pageable pageable);

    @Query("""
        SELECT n FROM NewsPost n WHERE n.slug = :slug
        AND n.publishedAt IS NOT NULL AND n.hidden = FALSE
        """)
    Optional<NewsPost> findBySlugPublic(@Param("slug") String slug);

    @Query("SELECT n FROM NewsPost n ORDER BY n.createdAt DESC")
    Page<NewsPost> findAllForAdmin(Pageable pageable);

    @Query("""
        SELECT n FROM NewsPost n
        WHERE n.publishedAt IS NOT NULL AND n.hidden = FALSE
        ORDER BY n.publishedAt DESC
        """)
    List<NewsPost> findLatest(Pageable pageable);
}
```

---

### 1.6. AccountRepository.java — Phần bổ sung (thêm vào class hiện tại)

```java
// Thêm vào AccountRepository.java

/** Kiểm tra email đã xác thực chưa. */
public boolean isEmailVerified(long accountId) {
    var count = db.queryForObject("""
        SELECT COUNT(*) FROM accounts
        WHERE id = ? AND email_verified_at IS NOT NULL
        """, Integer.class, accountId);
    return count != null && count > 0;
}

/** Thu hồi tất cả session của hộ bị khóa. */
public void revokeAllSessionsForWorkshop(long workshopId) {
    db.update("""
        DELETE FROM account_sessions
        WHERE account_id IN (
            SELECT id FROM accounts WHERE workshop_id = ?
        )
        """, workshopId);
}

/** Tạo tài khoản Buyer (trả id mới). */
public long createBuyer(String username, String passwordHash,
                        String displayName, String email, String phone) {
    Long id = db.queryForObject("""
        INSERT INTO accounts(username, password_hash, role, display_name, email, phone)
        VALUES (?, ?, 'BUYER', ?, ?, ?) RETURNING id
        """, Long.class, username, passwordHash, displayName, email, phone);
    if (id == null) throw new IllegalStateException("Không lấy được id tài khoản mới.");
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

/** Cập nhật findActiveSession — THAY THẾ method cũ — thêm locked_at IS NULL */
public Optional<Actor> findActiveSession(String tokenHash) {
    return db.query("""
        SELECT a.id, a.role, a.workshop_id FROM accounts a
        JOIN account_sessions s ON s.account_id = a.id
        LEFT JOIN workshops w ON w.id = a.workshop_id
        WHERE s.token_hash = ? AND s.expires_at > now()
          AND (a.role != 'ARTISAN' OR (w.approved = TRUE AND w.locked_at IS NULL))
        """,
        (rs, row) -> new Actor(rs.getLong("id"), rs.getString("role"),
                               rs.getObject("workshop_id", Long.class)),
        tokenHash).stream().findFirst();
}

/** Stats cho Admin dashboard. */
public long countBuyers() {
    var c = db.queryForObject("SELECT COUNT(*) FROM accounts WHERE role='BUYER'", Long.class);
    return c != null ? c : 0L;
}
```

---

### 1.7. ProductRepository.java — Phần bổ sung (thêm vào interface JPA hiện tại)

```java
// Thêm vào ProductRepository (JPA interface)

/** Tìm theo slug — public (không ẩn). */
@Query("""
    SELECT p FROM Product p
    JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.slug = :slug AND p.hidden = FALSE
    """)
Optional<Product> findBySlugPublic(@Param("slug") String slug);

/** Tìm theo slug — artisan (kể cả ẩn, kiểm tra ownership ở Service). */
@Query("""
    SELECT p FROM Product p
    JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.slug = :slug
    """)
Optional<Product> findBySlug(@Param("slug") String slug);

/** Phân trang có filter — public. */
@Query(value = """
    SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.hidden = FALSE
      AND (:workshopId IS NULL OR p.workshop.id = :workshopId)
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """,
    countQuery = """
    SELECT COUNT(p) FROM Product p
    WHERE p.hidden = FALSE
      AND (:workshopId IS NULL OR p.workshop.id = :workshopId)
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
Page<Product> findFiltered(
    @Param("workshopId") Long workshopId,
    @Param("categoryId") Long categoryId,
    Pageable pageable);

/** Tất cả sản phẩm của xưởng (artisan). */
@Query("""
    SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category
    WHERE p.workshop.id = :workshopId ORDER BY p.createdAt DESC
    """)
List<Product> findByWorkshopAll(@Param("workshopId") long workshopId);

/** Kiểm tra slug đã tồn tại chưa (dùng trong SlugUtils). */
boolean existsBySlug(String slug);

/** Nổi bật. */
@Query("SELECT p FROM Product p JOIN FETCH p.workshop JOIN FETCH p.category WHERE p.featured = TRUE AND p.hidden = FALSE")
List<Product> findFeatured();

@Modifying
@Query("UPDATE Product p SET p.hidden = TRUE WHERE p.id = :id")
void hide(@Param("id") long id);

@Modifying
@Query("UPDATE Product p SET p.featured = FALSE WHERE p.featured = TRUE")
void clearAllFeatured();

@Modifying
@Query("UPDATE Product p SET p.featured = :v WHERE p.id = :id")
void setFeatured(@Param("id") long id, @Param("v") boolean v);
```

---

## PHẦN 2 — SEED DATA CẬP NHẬT

### 2.1. catalog.sql — Phiên bản V5 (thêm dữ liệu mẫu cho cột mới)

**File:** `backend/src/main/resources/db/seed/catalog.sql`

Thêm vào cuối file hiện tại (sau các INSERT đã có):

```sql
-- =====================================================
-- Seed bổ sung V5: Dữ liệu mẫu cho cột mới
-- Chạy sau khi V5 đã migrate thành công.
-- Idempotent: dùng ON CONFLICT và SET chỉ khi NULL.
-- =====================================================

-- Bổ sung thông tin hồ sơ cho 2 xưởng mẫu
UPDATE workshops SET
    description      = COALESCE(description,
        'Xưởng Men Lam theo đuổi phong cách gốm Bát Tràng truyền thống với nét men lam đặc trưng. Mỗi sản phẩm đều được vẽ tay và nung qua nhiều lần để đạt màu sắc đồng đều.'),
    family_history   = COALESCE(family_history,
        'Nghề gốm trong gia đình đã truyền qua bốn thế hệ từ những năm 1920. Người sáng lập là cụ Nguyễn Văn Thịnh, học nghề tại làng gốm Bát Tràng và mở xưởng vào năm 1948.'),
    address          = COALESCE(address, 'Xóm 5, thôn Bát Tràng, xã Bát Tràng, huyện Gia Lâm, Hà Nội'),
    latitude         = COALESCE(latitude,  20.9638),
    longitude        = COALESCE(longitude, 105.9050),
    accepts_visitors = COALESCE(accepts_visitors, TRUE),
    featured         = TRUE
WHERE slug = 'xuong-men-lam';

UPDATE workshops SET
    description      = COALESCE(description,
        'Xưởng Đất Nung chuyên về gốm đất nung mộc — sản phẩm giữ nguyên màu đất nung đỏ nâu ấm, không phủ men. Phù hợp cho không gian thiền, cà phê và cắm hoa.'),
    family_history   = COALESCE(family_history,
        'Thành lập năm 2003 bởi chị Trần Thị Mai sau khi học kỹ thuật nung đất tại Nhật Bản. Xưởng kết hợp kỹ thuật truyền thống Việt Nam với thiết kế tối giản hiện đại.'),
    address          = COALESCE(address, 'Xóm 2, thôn Bát Tràng, xã Bát Tràng, huyện Gia Lâm, Hà Nội'),
    latitude         = COALESCE(latitude,  20.9612),
    longitude        = COALESCE(longitude, 105.9031),
    accepts_visitors = COALESCE(accepts_visitors, FALSE)
WHERE slug = 'xuong-dat-nung';

-- Giờ mở cửa mẫu cho xuong-men-lam (T2–T7: 8:00–17:30, CN nghỉ)
INSERT INTO workshop_hours(workshop_id, day_of_week, open_time, close_time)
SELECT w.id, d.dow, d.open_t::time, d.close_t::time
FROM workshops w,
     (VALUES (1,'08:00','17:30'), (2,'08:00','17:30'), (3,'08:00','17:30'),
             (4,'08:00','17:30'), (5,'08:00','17:30'), (6,'08:00','16:00')
     ) AS d(dow, open_t, close_t)
WHERE w.slug = 'xuong-men-lam'
ON CONFLICT (workshop_id, day_of_week) DO NOTHING;

-- Bổ sung giá tham khảo cho sản phẩm mẫu
UPDATE products SET
    reference_price = COALESCE(reference_price, 180000),
    price_visible   = COALESCE(price_visible, TRUE),
    featured        = TRUE
WHERE slug = 'coc-men-lam';

UPDATE products SET
    reference_price = COALESCE(reference_price, 320000),
    price_visible   = COALESCE(price_visible, TRUE)
WHERE slug = 'binh-men-lam';

UPDATE products SET
    reference_price = COALESCE(reference_price, 95000),
    price_visible   = COALESCE(price_visible, TRUE)
WHERE slug = 'coc-dat-nung';

-- Công đoạn sản xuất mẫu cho coc-men-lam
INSERT INTO process_steps(product_id, position, name, description)
SELECT p.id, s.pos, s.nm, s.desc
FROM products p,
     (VALUES
         (0, 'Chọn đất', 'Đất sét từ mỏ Bát Tràng được lọc bỏ tạp chất và ủ ít nhất 3 tháng để đạt độ dẻo tối ưu.'),
         (1, 'Tạo hình', 'Thợ bàn xoay nặn thành hình cốc, chuẩn hóa độ dày thành 3–4mm trên khắp mặt.'),
         (2, 'Phơi khô', 'Phơi trong bóng mát 2–3 ngày để tránh nứt vỡ. Không dùng nhiệt nhân tạo ở giai đoạn này.'),
         (3, 'Vẽ men lam', 'Nghệ nhân vẽ tay họa tiết bằng bút lông và phẩm màu coban pha nước. Mỗi cốc mất 20–30 phút.'),
         (4, 'Phủ men', 'Nhúng toàn bộ cốc vào bể men trắng; để khô trước khi vào lò.'),
         (5, 'Nung lò', 'Nung ở 1280°C trong 18 tiếng. Nhiệt độ tăng dần từ 200°C theo chu kỳ 2 tiếng/100°C.')
     ) AS s(pos, nm, desc)
WHERE p.slug = 'coc-men-lam'
ON CONFLICT (product_id, position) DO NOTHING;

-- Lô mẫu
INSERT INTO batches(product_id, workshop_id, code, public_code, name, description,
                   kind, quantity, unit, reference_price, price_visible,
                   supply_capacity, available_from, visibility)
SELECT p.id, p.workshop_id,
       'LOT-2026-001', 'bt2026001',
       'Lô cốc men lam Tết Đinh Mùi',
       'Lô sản xuất giới hạn dịp Tết 2026. Mỗi cốc được vẽ tay họa tiết hoa đào đặc biệt.',
       'IN_STOCK', 200, 'cái', 220000, TRUE,
       '200–500 cái/tháng', '2026-01-01', 'PUBLIC'
FROM products p WHERE p.slug = 'coc-men-lam'
ON CONFLICT (public_code) DO NOTHING;

-- Mốc sản xuất cho lô mẫu
INSERT INTO batch_milestones(batch_id, process_step_id, title, note, happened_on)
SELECT b.id, ps.id,
       'Chọn và ủ đất', 'Nhập 500kg đất sét từ mỏ, bắt đầu ủ cho lô Tết.', '2025-10-15'
FROM batches b
JOIN products p ON p.id = b.product_id
LEFT JOIN process_steps ps ON ps.product_id = p.id AND ps.position = 0
WHERE b.public_code = 'bt2026001'
ON CONFLICT DO NOTHING;

INSERT INTO batch_milestones(batch_id, process_step_id, title, note, happened_on)
SELECT b.id, ps.id,
       'Hoàn thành tạo hình', '200 cốc đã tạo hình xong, đang phơi khô.', '2025-11-20'
FROM batches b
JOIN products p ON p.id = b.product_id
LEFT JOIN process_steps ps ON ps.product_id = p.id AND ps.position = 1
WHERE b.public_code = 'bt2026001'
ON CONFLICT DO NOTHING;

INSERT INTO batch_milestones(batch_id, process_step_id, title, note, happened_on)
SELECT b.id, ps.id,
       'Vẽ men lam hoàn thành', 'Toàn bộ 200 cốc đã được vẽ tay họa tiết hoa đào.', '2025-12-10'
FROM batches b
JOIN products p ON p.id = b.product_id
LEFT JOIN process_steps ps ON ps.product_id = p.id AND ps.position = 3
WHERE b.public_code = 'bt2026001'
ON CONFLICT DO NOTHING;

-- Seed admin và buyer mẫu (password: Demo@2026)
-- BCrypt hash của 'Demo@2026' — tạo bằng: BCryptPasswordEncoder.encode("Demo@2026")
-- Thay hash bên dưới bằng hash thật khi chạy lần đầu
-- Hoặc dùng script: python3 -c "import bcrypt; print(bcrypt.hashpw(b'Demo@2026', bcrypt.gensalt()).decode())"
INSERT INTO accounts(username, password_hash, role, display_name, email, email_verified_at)
VALUES
    ('admin', '$2a$12$PLACEHOLDER_HASH_REPLACE_ME', 'ADMIN', 'Quản trị viên', 'admin@vcraft.local', now()),
    ('buyer01', '$2a$12$PLACEHOLDER_HASH_REPLACE_ME', 'BUYER', 'Nguyễn Văn Mua', 'buyer01@vcraft.local', now())
ON CONFLICT (username) DO NOTHING;
```

> **Lưu ý:** Thay `$2a$12$PLACEHOLDER_HASH_REPLACE_ME` bằng BCrypt hash thật của mật khẩu nhóm chọn. Chạy:
> ```bash
> cd backend
> mvn compile
> mvn exec:java -Dexec.mainClass="vn.vcraft.backend.DevHashPassword" -Dexec.args="Demo@2026"
> ```
> Hoặc dùng `AuthService` để tạo qua endpoint `/api/admin/workshops`.

---

## PHẦN 3 — CẤU TRÚC FRONTEND ĐẦY ĐỦ

Frontend hiện tại dồn toàn bộ trong 1 file `main.jsx` và `styles.css`. Phần này thiết kế cấu trúc module hóa cần tạo.

### 3.1. Cấu trúc thư mục đề xuất

```
frontend/src/
├── api/
│   ├── client.js          ← fetch wrapper (auth header, error handling)
│   ├── auth.js            ← login, logout, register, verifyEmail
│   ├── workshops.js       ← list, detail, map, artisan CRUD
│   ├── products.js        ← catalog, detail, artisan CRUD
│   ├── batches.js         ← public + artisan
│   ├── quotes.js          ← buyer + artisan quote flows
│   ├── media.js           ← upload, delete
│   └── news.js            ← public + admin
│
├── auth/
│   ├── AuthContext.jsx    ← React Context: user, login, logout
│   └── RequireAuth.jsx    ← Route guard theo role
│
├── components/
│   ├── Layout.jsx         ← Header + Footer + shell
│   ├── PagedList.jsx      ← Pagination reusable
│   ├── ImageUpload.jsx    ← Upload + preview ảnh
│   ├── MediaGallery.jsx   ← Hiển thị danh sách ảnh
│   ├── ErrorState.jsx     ← Lỗi + nút thử lại
│   ├── LoadingState.jsx   ← Skeleton / spinner
│   └── SlugLink.jsx       ← Link theo slug
│
├── pages/
│   ├── Home.jsx
│   ├── WorkshopList.jsx
│   ├── WorkshopDetail.jsx
│   ├── Map.jsx            ← Leaflet map
│   ├── ProductList.jsx
│   ├── ProductDetail.jsx
│   ├── BatchDetail.jsx    ← Landing page lô (public, từ QR)
│   ├── NewsList.jsx
│   ├── NewsDetail.jsx
│   │
│   ├── auth/
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   └── VerifyEmail.jsx
│   │
│   ├── buyer/
│   │   ├── QuoteRequestCreate.jsx
│   │   ├── QuoteRequestList.jsx
│   │   └── QuoteRequestDetail.jsx  ← xem báo giá, chấp nhận
│   │
│   ├── artisan/
│   │   ├── WorkshopProfile.jsx     ← sửa hồ sơ, giờ, chứng nhận
│   │   ├── ProductManage.jsx       ← CRUD sản phẩm + công đoạn
│   │   ├── BatchManage.jsx         ← CRUD lô + mốc + QR
│   │   ├── QuoteInbox.jsx          ← inbox yêu cầu phù hợp
│   │   └── QuoteDetail.jsx         ← gửi/sửa/rút báo giá
│   │
│   └── admin/
│       ├── Dashboard.jsx
│       ├── WorkshopAdmin.jsx       ← cấp, duyệt, khóa
│       ├── NewsAdmin.jsx           ← CRUD tin tức
│       ├── FeaturedAdmin.jsx       ← chọn nổi bật
│       └── TagAdmin.jsx            ← CRUD tag
│
└── main.jsx  ← chỉ còn BrowserRouter + Routes + AuthContext.Provider
```

### 3.2. api/client.js — Fetch wrapper

```javascript
// src/api/client.js
const BASE = '/api';

function getToken() {
  return sessionStorage.getItem('vcraft_token');
}

export function setToken(token) {
  if (token) sessionStorage.setItem('vcraft_token', token);
  else sessionStorage.removeItem('vcraft_token');
}

async function request(method, path, body, isMultipart = false) {
  const token = getToken();
  const headers = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!isMultipart && body) headers['Content-Type'] = 'application/json';

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: isMultipart ? body : (body ? JSON.stringify(body) : undefined),
  });

  // 204 No Content — không có body
  if (res.status === 204) return null;

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    // Ném object lỗi theo chuẩn ApiErrorResponse của backend
    const err = new Error(data.message || 'Lỗi không xác định');
    err.status = res.status;
    err.code = data.code;
    err.fieldErrors = data.fieldErrors;
    throw err;
  }

  return data;
}

export const api = {
  get:    (path)         => request('GET', path),
  post:   (path, body)   => request('POST', path, body),
  put:    (path, body)   => request('PUT', path, body),
  patch:  (path, body)   => request('PATCH', path, body),
  delete: (path)         => request('DELETE', path),
  upload: (path, formData) => request('POST', path, formData, true),
};
```

### 3.3. auth/AuthContext.jsx

```jsx
// src/auth/AuthContext.jsx
import React, { createContext, useContext, useEffect, useState } from 'react';
import { api, setToken } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(undefined); // undefined = chưa biết

  useEffect(() => {
    // Khôi phục session khi tải lại trang
    const token = sessionStorage.getItem('vcraft_token');
    if (!token) { setUser(null); return; }

    api.get('/me')
      .then(setUser)
      .catch(() => { setToken(null); setUser(null); });
  }, []);

  async function login(username, password) {
    const data = await api.post('/login', { username, password });
    setToken(data.token);
    setUser(data.account);
    return data.account;
  }

  async function logout() {
    await api.post('/logout').catch(() => {});
    setToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
```

### 3.4. auth/RequireAuth.jsx

```jsx
// src/auth/RequireAuth.jsx
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

/**
 * Bảo vệ route theo role.
 * <RequireAuth role="ARTISAN"><ArtisanPage /></RequireAuth>
 */
export function RequireAuth({ role, children }) {
  const { user } = useAuth();
  const location = useLocation();

  // Chờ load
  if (user === undefined) return <div className="notice">Đang tải…</div>;

  // Chưa đăng nhập
  if (!user) return <Navigate to="/dang-nhap" state={{ from: location }} replace />;

  // Sai role
  if (role && user.role !== role) return <Navigate to="/" replace />;

  return children;
}
```

### 3.5. main.jsx — Phiên bản module hóa

```jsx
// src/main.jsx — Chỉ còn routing
import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext.jsx';
import { RequireAuth } from './auth/RequireAuth.jsx';
import Layout from './components/Layout.jsx';
import './styles.css';

// Pages — lazy import để tách bundle
const Home              = React.lazy(() => import('./pages/Home.jsx'));
const WorkshopList      = React.lazy(() => import('./pages/WorkshopList.jsx'));
const WorkshopDetail    = React.lazy(() => import('./pages/WorkshopDetail.jsx'));
const Map               = React.lazy(() => import('./pages/Map.jsx'));
const ProductList       = React.lazy(() => import('./pages/ProductList.jsx'));
const ProductDetail     = React.lazy(() => import('./pages/ProductDetail.jsx'));
const BatchDetail       = React.lazy(() => import('./pages/BatchDetail.jsx'));
const NewsList          = React.lazy(() => import('./pages/NewsList.jsx'));
const Login             = React.lazy(() => import('./pages/auth/Login.jsx'));
const Register          = React.lazy(() => import('./pages/auth/Register.jsx'));
const VerifyEmail       = React.lazy(() => import('./pages/auth/VerifyEmail.jsx'));

// Artisan
const WorkshopProfile   = React.lazy(() => import('./pages/artisan/WorkshopProfile.jsx'));
const ProductManage     = React.lazy(() => import('./pages/artisan/ProductManage.jsx'));
const BatchManage       = React.lazy(() => import('./pages/artisan/BatchManage.jsx'));
const QuoteInbox        = React.lazy(() => import('./pages/artisan/QuoteInbox.jsx'));
const ArtisanQuoteDetail= React.lazy(() => import('./pages/artisan/QuoteDetail.jsx'));

// Buyer
const QuoteCreate       = React.lazy(() => import('./pages/buyer/QuoteRequestCreate.jsx'));
const QuoteList         = React.lazy(() => import('./pages/buyer/QuoteRequestList.jsx'));
const QuoteDetail       = React.lazy(() => import('./pages/buyer/QuoteRequestDetail.jsx'));

// Admin
const AdminDashboard    = React.lazy(() => import('./pages/admin/Dashboard.jsx'));
const WorkshopAdmin     = React.lazy(() => import('./pages/admin/WorkshopAdmin.jsx'));
const NewsAdmin         = React.lazy(() => import('./pages/admin/NewsAdmin.jsx'));

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <React.Suspense fallback={<div className="notice">Đang tải…</div>}>
          <Routes>
            <Route element={<Layout />}>
              {/* Public */}
              <Route path="/"                    element={<Home />} />
              <Route path="/ho-gia-dinh"         element={<WorkshopList />} />
              <Route path="/ho-gia-dinh/:slug"   element={<WorkshopDetail />} />
              <Route path="/ban-do"              element={<Map />} />
              <Route path="/san-pham"            element={<ProductList />} />
              <Route path="/san-pham/:idOrSlug"  element={<ProductDetail />} />
              <Route path="/batches/:publicCode" element={<BatchDetail />} />
              <Route path="/tin-tuc"             element={<NewsList />} />
              <Route path="/dang-nhap"           element={<Login />} />
              <Route path="/dang-ky"             element={<Register />} />
              <Route path="/xac-thuc-email"      element={<VerifyEmail />} />

              {/* Artisan */}
              <Route path="/ho-so-xuong" element={
                <RequireAuth role="ARTISAN"><WorkshopProfile /></RequireAuth>} />
              <Route path="/quan-ly/san-pham" element={
                <RequireAuth role="ARTISAN"><ProductManage /></RequireAuth>} />
              <Route path="/quan-ly/lo" element={
                <RequireAuth role="ARTISAN"><BatchManage /></RequireAuth>} />
              <Route path="/quan-ly/bao-gia" element={
                <RequireAuth role="ARTISAN"><QuoteInbox /></RequireAuth>} />
              <Route path="/quan-ly/bao-gia/:id" element={
                <RequireAuth role="ARTISAN"><ArtisanQuoteDetail /></RequireAuth>} />

              {/* Buyer */}
              <Route path="/yeu-cau/tao" element={
                <RequireAuth role="BUYER"><QuoteCreate /></RequireAuth>} />
              <Route path="/yeu-cau" element={
                <RequireAuth role="BUYER"><QuoteList /></RequireAuth>} />
              <Route path="/yeu-cau/:id" element={
                <RequireAuth role="BUYER"><QuoteDetail /></RequireAuth>} />

              {/* Admin */}
              <Route path="/admin" element={
                <RequireAuth role="ADMIN"><AdminDashboard /></RequireAuth>} />
              <Route path="/admin/ho-gia-dinh" element={
                <RequireAuth role="ADMIN"><WorkshopAdmin /></RequireAuth>} />
              <Route path="/admin/tin-tuc" element={
                <RequireAuth role="ADMIN"><NewsAdmin /></RequireAuth>} />

              {/* 404 */}
              <Route path="*" element={
                <section><h1>Không tìm thấy trang</h1></section>} />
            </Route>
          </Routes>
        </React.Suspense>
      </BrowserRouter>
    </AuthProvider>
  );
}

createRoot(document.getElementById('root')).render(<App />);
```

### 3.6. pages/BatchDetail.jsx — Landing page lô (quan trọng nhất với QR)

```jsx
// src/pages/BatchDetail.jsx
import React, { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { api } from '../api/client.js';

export default function BatchDetail() {
  const { publicCode } = useParams();
  const [searchParams]  = useSearchParams();
  const token           = searchParams.get('token'); // lô riêng tư

  const [batch, setBatch] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const path = `/batches/${publicCode}` + (token ? `?token=${token}` : '');
    api.get(path)
      .then(setBatch)
      .catch(err => setError(err.status === 404
        ? 'Không tìm thấy lô hàng.'
        : 'Không thể tải thông tin lô.'));
  }, [publicCode, token]);

  if (error) return <section><p className="notice">{error}</p></section>;
  if (!batch) return <section><p className="notice">Đang tải…</p></section>;

  return (
    <section className="batch-landing">
      {/* Thông tin lô */}
      <header>
        <p className="eyebrow">{batch.kind === 'IN_STOCK' ? 'SẵN KHO' : 'ĐẶT HÀNG'}</p>
        <h1>{batch.name || batch.code}</h1>
        {batch.description && <p>{batch.description}</p>}
        {batch.referencePrice && (
          <p className="price">
            {batch.referencePrice.toLocaleString('vi-VN')} ₫
            {batch.unit && ` / ${batch.unit}`}
          </p>
        )}
      </header>

      {/* Thông tin sản phẩm + xưởng */}
      <div className="batch-meta">
        <p>Sản phẩm: <strong>{batch.product?.name}</strong></p>
        <p>Xưởng: <a href={`/ho-gia-dinh/${batch.workshop?.slug}`}>
          {batch.workshop?.name}
        </a></p>
        {batch.quantity && <p>Số lượng: {batch.quantity} {batch.unit}</p>}
        {batch.availableFrom && (
          <p>Từ ngày: {new Date(batch.availableFrom).toLocaleDateString('vi-VN')}</p>
        )}
      </div>

      {/* Timeline mốc sản xuất */}
      {batch.milestones?.length > 0 && (
        <div className="timeline">
          <h2>Quá trình sản xuất</h2>
          <ol className="milestone-list">
            {batch.milestones.map(m => (
              <li key={m.id} className="milestone">
                <time dateTime={m.happenedOn}>
                  {new Date(m.happenedOn).toLocaleDateString('vi-VN')}
                </time>
                <div>
                  <strong>{m.title}</strong>
                  {m.processStepName && <span className="step-tag">{m.processStepName}</span>}
                  {m.note && <p>{m.note}</p>}
                  {/* Ảnh milestone */}
                  {m.images?.length > 0 && (
                    <div className="milestone-images">
                      {m.images.map(img => (
                        <img key={img.id} src={img.mediumUrl}
                             alt={m.title} loading="lazy" />
                      ))}
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* Quy trình của sản phẩm */}
      {batch.productSteps?.length > 0 && (
        <div className="process">
          <h2>Quy trình sản xuất {batch.product?.name}</h2>
          <ol>
            {batch.productSteps.map(s => (
              <li key={s.id}>
                <strong>{s.name}</strong>
                {s.description && <p>{s.description}</p>}
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* Ảnh lô */}
      {batch.images?.length > 0 && (
        <div className="batch-gallery">
          <h2>Hình ảnh</h2>
          <div className="grid">
            {batch.images.map(img => (
              <img key={img.id} src={img.mediumUrl}
                   alt={batch.name} loading="lazy" />
            ))}
          </div>
        </div>
      )}

      {/* QR — chỉ hiện nếu PUBLIC */}
      {batch.visibility === 'PUBLIC' && (
        <div className="qr-section">
          <p>Chia sẻ lô này:</p>
          <img
            src={`/api/batches/${publicCode}/qr.png`}
            alt="QR code lô hàng"
            width="200" height="200"
          />
        </div>
      )}
    </section>
  );
}
```

### 3.7. pages/Map.jsx — Bản đồ Leaflet

```jsx
// src/pages/Map.jsx
// Cần thêm leaflet vào package.json:
// npm install leaflet react-leaflet
// Thêm import CSS vào main.jsx: import 'leaflet/dist/leaflet.css';

import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { api } from '../api/client.js';

// Fix icon mặc định của Leaflet bị vỡ khi bundle với Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

export default function Map() {
  const [pins, setPins] = useState([]);
  const [filter, setFilter] = useState('all'); // 'all' | 'open'

  useEffect(() => {
    api.get('/workshops/map').then(setPins).catch(console.error);
  }, []);

  const filtered = filter === 'open'
    ? pins.filter(p => p.openNow === true)
    : pins;

  // Trung tâm làng Bát Tràng
  const center = [20.9625, 105.9040];

  return (
    <section>
      <h1>Bản đồ làng nghề Bát Tràng</h1>

      <div style={{ marginBottom: '12px' }}>
        <label>
          <input type="radio" name="filter" value="all"
                 checked={filter === 'all'}
                 onChange={() => setFilter('all')} />
          {' '}Tất cả xưởng ({pins.length})
        </label>
        {' '}
        <label>
          <input type="radio" name="filter" value="open"
                 checked={filter === 'open'}
                 onChange={() => setFilter('open')} />
          {' '}Đang mở cửa ({pins.filter(p => p.openNow === true).length})
        </label>
      </div>

      <MapContainer center={center} zoom={15}
                    style={{ height: '500px', width: '100%', borderRadius: '12px' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {filtered.map(pin => (
          <Marker key={pin.id}
                  position={[Number(pin.latitude), Number(pin.longitude)]}>
            <Popup>
              <div style={{ minWidth: '160px' }}>
                {pin.thumbnail && (
                  <img src={pin.thumbnail.thumbUrl} alt={pin.name}
                       style={{ width: '100%', borderRadius: '6px', marginBottom: '6px' }} />
                )}
                <strong>{pin.name}</strong>
                <br />
                {pin.openNow === true && <span style={{ color: 'green' }}>● Đang mở</span>}
                {pin.openNow === false && <span style={{ color: 'gray' }}>● Đóng cửa</span>}
                {pin.openNow === null && <span style={{ color: '#aaa' }}>Chưa cập nhật giờ</span>}
                {pin.address && <p style={{ fontSize: '0.85em', margin: '4px 0' }}>{pin.address}</p>}
                <a href={`/ho-gia-dinh/${pin.slug}`}
                   style={{ fontSize: '0.85em' }}>Xem hồ sơ →</a>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      <p style={{ marginTop: '8px', fontSize: '0.85em', color: '#666' }}>
        {filtered.length} xưởng hiển thị · Bản đồ: OpenStreetMap
      </p>
    </section>
  );
}
```

### 3.8. package.json — Thêm Leaflet

```json
{
  "name": "vcraft-frontend",
  "version": "0.0.1",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build"
  },
  "dependencies": {
    "leaflet": "1.9.4",
    "react": "19.2.0",
    "react-dom": "19.2.0",
    "react-leaflet": "4.2.1",
    "react-router-dom": "7.9.6"
  },
  "devDependencies": {
    "vite": "7.2.2"
  }
}
```

---

## PHẦN 4 — CÁC DTO CÒN THIẾU

```java
// dto/WorkshopHourResponse.java
public record WorkshopHourResponse(int dayOfWeek, String openTime, String closeTime) {}

// dto/WorkshopHourInput.java
public record WorkshopHourInput(
    @NotNull @Min(0) @Max(6) Integer dayOfWeek,
    @NotBlank @Pattern(regexp="^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
                       message="Định dạng giờ phải là HH:mm") String openTime,
    @NotBlank @Pattern(regexp="^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
                       message="Định dạng giờ phải là HH:mm") String closeTime
) {}

// dto/WorkshopClosureInput.java
public record WorkshopClosureInput(
    @NotNull LocalDate closedOn,
    @Size(max=200) String reason
) {}

// dto/CertificateResponse.java
public record CertificateResponse(
    Long id, String name, String issuer, LocalDate issuedOn, Instant createdAt) {}

// dto/CertificateInput.java
public record CertificateInput(
    @NotBlank @Size(max=200) String name,
    @Size(max=200) String issuer,
    LocalDate issuedOn,
    List<Long> mediaIds
) {}

// dto/AdminStatsResponse.java
public record AdminStatsResponse(
    long totalWorkshops, long approvedWorkshops,
    long totalProducts, long totalBatches,
    long totalQuoteRequests, long totalBuyers
) {}

// dto/FeaturedInput.java
public record FeaturedInput(
    List<Long> workshopIds,
    List<Long> productIds,
    List<Long> batchIds
) {}

// dto/LockInput.java
public record LockInput(@NotBlank @Size(max=500) String reason) {}

// dto/TagInput.java
public record TagInput(
    @NotBlank @Size(max=120)
    @Pattern(regexp="^[a-z0-9-]+$", message="Slug chỉ dùng chữ thường, số và dấu gạch ngang")
    String slug,
    @NotBlank @Size(max=120) String name
) {}

// dto/TagResponse.java
public record TagResponse(Long id, String slug, String name) {}

// dto/HomeResponse.java
public record HomeResponse(
    List<WorkshopSummaryResponse> featuredWorkshops,
    List<ProductSummaryResponse> featuredProducts,
    List<BatchSummaryResponse> featuredBatches,
    List<NewsResponse> latestNews
) {}

// dto/WorkshopSummaryResponse.java (sửa lại — thêm openNow)
public record WorkshopSummaryResponse(
    Long id, String slug, String name, String description,
    MediaResponse coverImage,
    Boolean acceptsVisitors, Boolean openNow
) {}

// dto/MapPinResponse.java
public record MapPinResponse(
    Long id, String slug, String name,
    java.math.BigDecimal latitude, java.math.BigDecimal longitude,
    MediaResponse thumbnail,
    Boolean acceptsVisitors, Boolean openNow,
    String address
) {}
```

---

## PHẦN 5 — CHECKLIST THỰC THI THEO THỨ TỰ

### Sprint 1 (Thắng · ~3 ngày · M1)

```
[ ] 1. Kiểm tra flyway_schema_history → xử lý V5 placeholder
[ ] 2. Viết V5 chính thức, chạy migration, xác nhận không lỗi
[ ] 3. Cập nhật Workshop.java Entity (thêm 10 field mới)
[ ] 4. Cập nhật Product.java Entity (thêm 5 field mới)
[ ] 5. Thêm pom.xml: ZXing + Thumbnailator + Tika + Mail
[ ] 6. Thêm application.properties: multipart, mail, app.base-url, media.storage-path
[ ] 7. Viết MediaStorage interface + LocalFileStorage
[ ] 8. Viết MediaRepository (JDBC)
[ ] 9. Viết MediaService + MediaController
[ ] 10. Viết OtpRepository + OtpService
[ ] 11. Mở rộng AccountRepository (+7 method, cập nhật findActiveSession)
[ ] 12. Mở rộng AuthService + AuthController (+register, verify, resend)
[ ] 13. Viết CorsConfig
[ ] 14. Cập nhật SecurityConfig (permitAll)
[ ] 15. Viết SlugUtils
[ ] 16. Viết V6__news.sql
[ ] 17. Chạy mvn package → xác nhận build xanh
[ ] 18. Test POST /api/register + POST /api/register/verify-email
[ ] 19. Test POST /api/media upload ảnh JPEG → 3 URL
[ ] 20. Test upload PDF → 415; ảnh > 5MB → 413
```

### Sprint 2 (Huy · M2A, song song với Dương)

```
[ ] 1. Mở rộng WorkshopRepository (listPublic, findBySlug, mapPins, updateProfile, certificates)
[ ] 2. Viết WorkshopHourRepository (replaceHours, isOpenNow, addClosure, deleteClosure)
[ ] 3. Mở rộng WorkshopService (listPublic, getBySlug, mapPins, updateProfile, updateHours, addClosure, addCertificate...)
[ ] 4. Viết WorkshopController (public)
[ ] 5. Mở rộng ArtisanWorkshopController (+8 endpoints)
[ ] 6. Mở rộng ProductRepository (findBySlugPublic, findFiltered, findByWorkshopAll, existsBySlug)
[ ] 7. Viết ProcessStepRepository (JPA)
[ ] 8. Mở rộng ProductService (create, update, hide, updateSteps, updateTags)
[ ] 9. Viết ArtisanProductController
[ ] 10. Mở rộng ProductController ({idOrSlug}, filter params, /steps)
[ ] 11. Viết tất cả DTO WS + PROD
[ ] 12. Frontend: AuthContext + RequireAuth + Layout
[ ] 13. Frontend: main.jsx module hóa + routing
[ ] 14. Frontend: WorkshopList, WorkshopDetail, Map (Leaflet)
[ ] 15. Frontend: ProductList (filter), ProductDetail (steps)
[ ] 16. Frontend: artisan/WorkshopProfile (sửa hồ sơ, giờ, chứng nhận)
[ ] 17. Frontend: artisan/ProductManage (CRUD + steps + tags + upload ảnh)
[ ] 18. Test: GET /api/workshops/map → ghim có tọa độ
[ ] 19. Test: Artisan sửa hồ sơ → reload → thấy thay đổi
[ ] 20. Test: giá ẩn → response không có referencePrice (null)
```

### Sprint 3 (Dương · M2B, song song)

```
[ ] 1. Viết Batch.java Entity + BatchMilestone.java Entity
[ ] 2. Viết ProcessStep.java Entity
[ ] 3. Viết BatchRepository (JPA)
[ ] 4. Viết BatchMilestoneRepository (JPA)
[ ] 5. Viết BatchService (create, update, getLandingPage, generatePublicQr, generateArtisanQr, generateShareToken, revokeShareToken, updateMilestones)
[ ] 6. Viết BatchController (public)
[ ] 7. Viết ArtisanBatchController
[ ] 8. Viết tất cả DTO Batch
[ ] 9. npm install leaflet react-leaflet vào frontend
[ ] 10. Frontend: Map.jsx (Leaflet pins + filter open/close)
[ ] 11. Frontend: BatchDetail.jsx (landing page từ QR)
[ ] 12. Frontend: artisan/BatchManage (CRUD + milestones + share-token + QR download)
[ ] 13. Test: POST /artisan/batches → 201 + public_code
[ ] 14. Test: GET /batches/{code}/qr.png → PNG 300×300
[ ] 15. Test quét QR điện thoại thật → landing page
[ ] 16. Test: Lô PRIVATE + sai token → 404
[ ] 17. Test: POST share-token → URL; DELETE share-token → 404
```

### Sprint 4 (Thắng · M2C Frontend + M3)

```
[ ] 1. Thêm isEmailVerified check vào QuoteRequestService.create()
[ ] 2. Frontend: artisan/QuoteInbox
[ ] 3. Frontend: artisan/QuoteDetail (gửi/sửa/rút báo giá, tiến độ)
[ ] 4. Frontend: buyer/QuoteRequestCreate
[ ] 5. Frontend: buyer/QuoteRequestList + QuoteRequestDetail
[ ] 6. Viết NewsPost.java Entity + NewsRepository
[ ] 7. Viết NewsService + NewsController (public)
[ ] 8. Mở rộng AdminService (lock/unlock/stats/featured/hậu kiểm/CRUD tag)
[ ] 9. Mở rộng AdminController
[ ] 10. AccountRepository.revokeAllSessionsForWorkshop()
[ ] 11. Frontend: admin/WorkshopAdmin + NewsAdmin + Dashboard
[ ] 12. Seed data V5 (catalog.sql bổ sung)
[ ] 13. Viết V6 news migration
[ ] 14. Test POST /admin/workshops/{id}/lock → session bị thu hồi ngay
[ ] 15. Test GET /api/home → featured + news
```

### Sprint 5 (Cả nhóm · M4)

```
[ ] 1. Seed dữ liệu "thật" trên VPS (5 hộ, 20+ sản phẩm, ảnh thật)
[ ] 2. Test toàn bộ Security Matrix (mỗi ô trong bảng Phần E Blueprint)
[ ] 3. Test concurrent accept (2 tab song song) → 1 × 200, 1 × 409
[ ] 4. Test toàn bộ Demo Script 13 bước
[ ] 5. Chạy Postman collection đủ endpoint
[ ] 6. EXPLAIN ANALYZE các query chính (products list, map pins)
[ ] 7. Backup DB + ảnh trước ngày thi
[ ] 8. Kiểm tra QR trên mạng di động thật (không phải localhost)
```
