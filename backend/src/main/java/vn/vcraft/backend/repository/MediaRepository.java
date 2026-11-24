package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import vn.vcraft.backend.dto.MediaResponse;

@Repository
public class MediaRepository {
    private final JdbcTemplate db;
    public MediaRepository(JdbcTemplate db) { this.db = db; }

    public record MediaRow(long id, long ownerAccountId,
                           String pathThumb, String pathMedium, String pathLarge) {}

    public long create(long ownerAccountId, String originalName, String contentType,
                       int sizeBytes, int width, int height,
                       String pathThumb, String pathMedium, String pathLarge) {
        return db.queryForObject("""
            INSERT INTO media(owner_account_id, original_name, content_type, size_bytes,
                             width, height, path_thumb, path_medium, path_large)
            VALUES (?,?,?,?,?,?,?,?,?) RETURNING id
            """, Long.class,
                ownerAccountId, originalName, contentType, sizeBytes,
                width, height, pathThumb, pathMedium, pathLarge);
    }

    public Optional<MediaRow> findById(long id) {
        return db.query("""
            SELECT id, owner_account_id, path_thumb, path_medium, path_large
            FROM media WHERE id = ?
            """,
                (rs, r) -> new MediaRow(rs.getLong("id"), rs.getLong("owner_account_id"),
                        rs.getString("path_thumb"), rs.getString("path_medium"), rs.getString("path_large")),
                id).stream().findFirst();
    }

    public boolean isLinked(long mediaId) {
        var count = db.queryForObject(
                "SELECT (SELECT COUNT(*) FROM media_links WHERE media_id = ?) + (SELECT COUNT(*) FROM workshops WHERE cover_media_id = ?)", Integer.class, mediaId, mediaId);
        return count != null && count > 0;
    }

    public void delete(long mediaId) {
        db.update("DELETE FROM media WHERE id = ?", mediaId);
    }

    /** Thay thế toàn bộ media_links của một entity. */
    public void replaceLinks(String entityType, long entityId, java.util.List<Long> mediaIds) {
        db.update("DELETE FROM media_links WHERE entity_type = ? AND entity_id = ?",
                entityType, entityId);
        for (int i = 0; i < mediaIds.size(); i++) {
            db.update("""
                INSERT INTO media_links(media_id, entity_type, entity_id, position)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (media_id, entity_type, entity_id) DO UPDATE SET position = EXCLUDED.position
                """, mediaIds.get(i), entityType, entityId, i);
        }
    }

    /** Lấy ảnh của một entity, sắp theo position. */
    public java.util.List<MediaResponse> findByEntity(String entityType, long entityId,
                                                      String baseUrl) {
        return db.query("""
            SELECT m.id, m.path_thumb, m.path_medium, m.path_large, m.width, m.height
            FROM media m JOIN media_links ml ON ml.media_id = m.id
            WHERE ml.entity_type = ? AND ml.entity_id = ?
            ORDER BY ml.position
            """,
                (rs, r) -> new vn.vcraft.backend.dto.MediaResponse(
                        rs.getLong("id"),
                        baseUrl + "/media/" + rs.getString("path_thumb"),
                        baseUrl + "/media/" + rs.getString("path_medium"),
                        baseUrl + "/media/" + rs.getString("path_large"),
                        rs.getInt("width"), rs.getInt("height")),
                entityType, entityId);
    }
}
