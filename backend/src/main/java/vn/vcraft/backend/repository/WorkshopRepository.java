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
        SELECT w.id, w.name, w.slug, w.approved, a.username
        FROM workshops w LEFT JOIN accounts a ON a.workshop_id = w.id ORDER BY w.id
  }

  public WorkshopResponse createPending(String slug, String name) {
INSERT INTO workshops(slug, name, approved) VALUES (?, ?, false) RETURNING id, name, approved
  }

  public Optional<WorkshopResponse> approve(long id) {
  }

  public Optional<WorkshopResponse> rename(long id, String name) {
  }
}
