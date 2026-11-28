package vn.vcraft.backend.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.model.Actor;

@Repository
public class AccountRepository {
    private final JdbcTemplate db;
    @org.springframework.beans.factory.annotation.Value("${auth.session-hours:168}")
    private int sessionHours = 168;

    public AccountRepository(JdbcTemplate db) {
        this.db = db;
    }

    public record Credentials(long id, String passwordHash, String role) {}

    public Optional<Credentials> findActiveCredentials(String username) {
        return db.query("""
                SELECT a.id, a.password_hash, a.role
                FROM accounts a LEFT JOIN workshops w ON w.id = a.workshop_id
                WHERE a.username = ? AND (a.role != 'ARTISAN' OR (w.approved = TRUE AND w.locked_at IS NULL))
                """, (rs, row) -> new Credentials(rs.getLong("id"), rs.getString("password_hash"),
                rs.getString("role")), username).stream().findFirst();
    }

    public void createSession(long accountId, String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE expires_at <= now()");
        db.update("INSERT INTO account_sessions(token_hash, account_id, expires_at) VALUES (?, ?, now() + (? * interval '1 hour'))",
                tokenHash, accountId, sessionHours);
    }

    public void revokeSession(String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE token_hash = ?", tokenHash);
    }

    public void addArtisan(String username, String passwordHash, long workshopId) {
        db.update("INSERT INTO accounts(username, password_hash, role, workshop_id) VALUES (?, ?, 'ARTISAN', ?)",
                username, passwordHash, workshopId);
    }

    public void seedAccount(String username, String passwordHash, String role) {
        db.update("INSERT INTO accounts(username, password_hash, role) VALUES (?, ?, ?) ON CONFLICT(username) DO NOTHING",
                username, passwordHash, role);
    }

    public void seedArtisan(String slug, String passwordHash) {
        db.update("""
                INSERT INTO accounts(username, password_hash, role, workshop_id)
                SELECT ?, ?, 'ARTISAN', id FROM workshops WHERE slug = ? ON CONFLICT DO NOTHING
                """, slug, passwordHash, slug);
    }

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
}
