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
                    rs.getBoolean("approved"), (rs.getTimestamp("locked_at") == null ? null : rs.getTimestamp("locked_at").toInstant()),
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

  public List<WorkshopSummaryResponse> listFeatured(String baseUrl) {
    return listPublic(50,0,baseUrl).stream().filter(w -> db.queryForObject(
            "SELECT featured FROM workshops WHERE id=?",Boolean.class,w.id())).limit(3).toList();
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
                    (rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant()),
                    (rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()),
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
                    (rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant()),
                    (rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()),
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
                    (rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant())),
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
                    (rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant())),
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
  public Optional<WorkshopResponse> rename(long id, String name) {
    return db.query(
        "UPDATE workshops SET name = ? WHERE id = ? AND approved RETURNING id, name, slug",
        (rs, row) -> new WorkshopResponse(
            rs.getLong("id"), rs.getString("name"), rs.getString("slug"), null, null),
        name, id).stream().findFirst();
  }
  public Optional<ContactResponse> updateContact(long id, String email, String phone) {
    return db.query(
        """
        UPDATE workshops SET contact_email = ?, phone = ?
        WHERE id = ? AND approved RETURNING name, contact_email, phone
        """,
        (rs, row) -> new ContactResponse(
            rs.getString("name"), rs.getString("contact_email"), rs.getString("phone")),
        email, phone, id).stream().findFirst();
  }
    public boolean existsBySlug(String slug) {
        return db.queryForObject("SELECT count(*) FROM workshops WHERE slug=?", Long.class, slug)>0;
    }
}
