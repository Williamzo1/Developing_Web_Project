package vn.vcraft.backend.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MediaRepository {
  private final JdbcTemplate db;

  public MediaRepository(JdbcTemplate db) {
    this.db = db;
  }

  /** Khóa lưu trữ của ba biến thể một ảnh. */
  public record Stored(long id, String pathThumb, String pathMedium, String pathLarge) {}

  public long insert(
          long ownerAccountId,
          String originalName,
          String contentType,
          int sizeBytes,
          int width,
          int height,
          String pathThumb,
          String pathMedium,
          String pathLarge) {
    Long id =
            db.queryForObject(
                    """
                    INSERT INTO media (owner_account_id, original_name, content_type, size_bytes,
                                       width, height, path_thumb, path_medium, path_large)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """,
                    Long.class,
                    ownerAccountId,
                    originalName,
                    contentType,
                    sizeBytes,
                    width,
                    height,
                    pathThumb,
                    pathMedium,
                    pathLarge);
    return id;
  }

  /** Chỉ trả ảnh khi đúng chủ sở hữu; ảnh của người khác coi như không tồn tại. */
  public Optional<Stored> findOwned(long id, long ownerAccountId) {
    return db
            .query(
                    """
                    SELECT id, path_thumb, path_medium, path_large
                    FROM media WHERE id = ? AND owner_account_id = ?
                    """,
                    (rs, row) ->
                            new Stored(
                                    rs.getLong("id"),
                                    rs.getString("path_thumb"),
                                    rs.getString("path_medium"),
                                    rs.getString("path_large")),
                    id,
                    ownerAccountId)
            .stream()
            .findFirst();
  }

  public void delete(long id) {
    db.update("DELETE FROM media WHERE id = ?", id);
  }
}
