package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public class OtpRepository {
    private final JdbcTemplate db;

    public OtpRepository(JdbcTemplate db) { this.db = db; }

    public void invalidateExisting(long accountId, String purpose) {
        db.update("""
            UPDATE email_otps SET used_at = now()
            WHERE account_id = ? AND purpose = ? AND used_at IS NULL
            """, accountId, purpose);
    }

    public void create(long accountId, String otpHash, String purpose, Instant expiresAt) {
        db.update("""
            INSERT INTO email_otps(account_id, otp_hash, purpose, expires_at)
            VALUES (?, ?, ?, ?)
            """, accountId, otpHash, purpose, java.sql.Timestamp.from(expiresAt));
    }

    /** Đánh dấu OTP đã dùng nếu hợp lệ. Trả true nếu thành công. */
    public boolean markUsed(long accountId, String otpHash, String purpose, Instant now) {
        int rows = db.update("""
            UPDATE email_otps
            SET used_at = now()
            WHERE account_id = ? AND otp_hash = ? AND purpose = ?
              AND expires_at > ? AND used_at IS NULL
            """, accountId, otpHash, purpose, java.sql.Timestamp.from(now));
        return rows > 0;
    }
}
