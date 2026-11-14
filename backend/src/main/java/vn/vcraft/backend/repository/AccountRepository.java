package vn.vcraft.backend.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.model.Actor;

@Repository
public class AccountRepository {
    private final JdbcTemplate db;

    public AccountRepository(JdbcTemplate db) {
        this.db = db;
    }

    public record Credentials(long id, String passwordHash, String role) {}

    public Optional<Credentials> findActiveCredentials(String username) {
        return db.query("""
                SELECT a.id, a.password_hash, a.role
                FROM accounts a LEFT JOIN workshops w ON w.id = a.workshop_id
                WHERE a.username = ? AND (w.id IS NULL OR w.approved)
                """, (rs, row) -> new Credentials(rs.getLong("id"), rs.getString("password_hash"),
                rs.getString("role")), username).stream().findFirst();
    }

    public Optional<Actor> findActiveSession(String tokenHash) {
        return db.query("""
                SELECT a.id, a.role, a.workshop_id FROM accounts a
                JOIN account_sessions s ON s.account_id = a.id
                LEFT JOIN workshops w ON w.id = a.workshop_id
                WHERE s.token_hash = ? AND s.expires_at > now() AND (w.id IS NULL OR w.approved)
                """, (rs, row) -> new Actor(rs.getLong("id"), rs.getString("role"),
                rs.getObject("workshop_id", Long.class)), tokenHash).stream().findFirst();
    }

    public void createSession(long accountId, String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE expires_at <= now()");
        db.update("INSERT INTO account_sessions(token_hash, account_id, expires_at) VALUES (?, ?, now() + interval '8 hours')",
                tokenHash, accountId);
    }

    public void revokeSession(String tokenHash) {
        db.update("DELETE FROM account_sessions WHERE token_hash = ?", tokenHash);
    }

    public String lockPassword(long accountId) {
        return db.queryForObject("SELECT password_hash FROM accounts WHERE id = ? FOR UPDATE", String.class, accountId);
    }

    public void lockWorkshopAccount(long workshopId) {
        db.query("SELECT id FROM accounts WHERE workshop_id = ? FOR UPDATE",
                (rs, row) -> rs.getLong(1), workshopId);
    }

    public void changePassword(long accountId, String hash) {
        db.update("UPDATE accounts SET password_hash = ? WHERE id = ?", hash, accountId);
        db.update("DELETE FROM account_sessions WHERE account_id = ?", accountId);
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

    public void revokeWorkshopSessions(long workshopId) {
        db.update("""
                DELETE FROM account_sessions WHERE account_id IN (
                    SELECT id FROM accounts WHERE workshop_id = ?
                )
                """, workshopId);
    }
}
