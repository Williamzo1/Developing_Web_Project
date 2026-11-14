package vn.vcraft.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import vn.vcraft.backend.dto.CatalogOption;
import vn.vcraft.backend.dto.CatalogQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.dto.WorkshopProfile;
import vn.vcraft.backend.dto.WorkshopProfileInput;
import vn.vcraft.backend.dto.WorkshopPin;
import vn.vcraft.backend.dto.WorkshopResponse;

@Repository
public class WorkshopRepository {
    private static final String PROFILE_COLUMNS = """
            slug, name, description, family_history, address, phone, contact_email,
            facebook_url, latitude, longitude, approved
            """;
    private static final RowMapper<WorkshopProfile> PROFILE = (rs, row) -> new WorkshopProfile(
            rs.getString("slug"), rs.getString("name"), rs.getString("description"),
            rs.getString("family_history"), rs.getString("address"), rs.getString("phone"),
            rs.getString("contact_email"), rs.getString("facebook_url"),
            rs.getObject("latitude", Double.class), rs.getObject("longitude", Double.class),
            rs.getBoolean("approved"));
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

    public Optional<WorkshopResponse> suspend(long id) {
        return db.query("UPDATE workshops SET approved = false WHERE id = ? RETURNING id, name, slug",
                (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                rs.getString("slug"), false, null), id).stream().findFirst();
    }

    public PageResponse<CatalogOption> publicList(CatalogQuery query) {
        String filter = " WHERE approved AND strpos(lower(name), lower(?)) > 0";
        String keyword = query.getQ() == null ? "" : query.getQ().strip();
        long total = db.queryForObject("SELECT count(*) FROM workshops" + filter, Long.class, keyword);
        int page = query.getPage();
        int size = query.getSize();
        int pages = (int) Math.ceil((double) total / size);
        var items = db.query("SELECT slug, name FROM workshops" + filter
                        + " ORDER BY name, id LIMIT ? OFFSET ?",
                (rs, row) -> new CatalogOption(rs.getString("slug"), rs.getString("name")),
                keyword, size, (long) page * size);
        return new PageResponse<>(items, page, size, total, pages, page == 0, page + 1 >= pages);
    }

    public Optional<WorkshopProfile> publicProfile(String slug) {
        return db.query("SELECT " + PROFILE_COLUMNS + " FROM workshops WHERE slug = ? AND approved",
                PROFILE, slug).stream().findFirst();
    }

    public List<WorkshopPin> mapPins() {
        return db.query("""
                SELECT slug, name, description, latitude, longitude FROM workshops
                WHERE approved AND latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY id
                """, (rs, row) -> new WorkshopPin(rs.getString("slug"), rs.getString("name"),
                rs.getString("description"), rs.getDouble("latitude"), rs.getDouble("longitude")));
    }

    public Optional<WorkshopProfile> ownProfile(long id) {
        return db.query("SELECT " + PROFILE_COLUMNS + " FROM workshops WHERE id = ? AND approved",
                PROFILE, id).stream().findFirst();
    }

    public Optional<WorkshopProfile> updateProfile(long id, WorkshopProfileInput input) {
        return db.query("""
                UPDATE workshops SET name = ?, description = ?, family_history = ?, address = ?,
                    phone = ?, contact_email = ?, facebook_url = ?, latitude = ?, longitude = ?
                WHERE id = ? AND approved RETURNING
                """ + PROFILE_COLUMNS, PROFILE, input.name().strip(), input.description(),
                input.familyHistory(), input.address(), input.phone(), input.contactEmail(), input.facebookUrl(),
                input.latitude(), input.longitude(), id).stream().findFirst();
    }

    public Optional<WorkshopResponse> rename(long id, String name) {
        return db.query("UPDATE workshops SET name = ? WHERE id = ? AND approved RETURNING id, name, slug",
                (rs, row) -> new WorkshopResponse(rs.getLong("id"), rs.getString("name"),
                rs.getString("slug"), null, null), name, id).stream().findFirst();
    }
}
