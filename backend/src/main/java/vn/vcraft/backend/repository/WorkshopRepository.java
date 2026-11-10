package vn.vcraft.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.WorkshopResponse;

@Repository
public class WorkshopRepository {
    private final JdbcTemplate db;

    public WorkshopRepository(JdbcTemplate db) {
        this.db = db;
    }

    public List<WorkshopResponse> list() {
        return db.query("""
                SELECT w.id, w.name, w.slug, w.approved, a.username
                FROM workshops w LEFT JOIN accounts a ON a.workshop_id = w.id ORDER BY w.id
                """, (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                rs.getString("slug"), rs.getBoolean("approved"), rs.getString("username")));
    }

    public WorkshopResponse createPending(String slug, String name) {
        return db.queryForObject("""
                INSERT INTO workshops(slug, name, approved) VALUES (?, ?, false) RETURNING id, name, approved
                """, (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                null, rs.getBoolean("approved"), null), slug, name);
    }

    public Optional<WorkshopResponse> approve(long id) {
        return db.query("UPDATE workshops SET approved = true WHERE id = ? RETURNING id, name, approved",
                (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                null, rs.getBoolean("approved"), null), id).stream().findFirst();
    }

    public Optional<WorkshopResponse> rename(long id, String name) {
        return db.query("UPDATE workshops SET name = ? WHERE id = ? AND approved RETURNING id, name, slug",
                (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                rs.getString("slug"), null, null), name, id).stream().findFirst();
    }
}
