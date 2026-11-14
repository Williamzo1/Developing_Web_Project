package vn.vcraft.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.BatchData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;

@Repository
public class BatchRepository {
    private static final String SELECT = """
            SELECT b.*, p.slug AS product_slug, p.name AS product_name,
                w.slug AS workshop_slug, w.name AS workshop_name, w.family_history
            """;
    private static final String FROM = """
            FROM batches b JOIN products p ON p.id = b.product_id
            JOIN workshops w ON w.id = p.workshop_id
            """;
    private static final String VISIBLE = "w.approved AND NOT p.hidden AND NOT p.admin_hidden AND NOT b.hidden";
    private static final String BATCH_VISIBILITY = "b.visibility = 'PUBLIC'";
    private static final RowMapper<Milestone> MILESTONE = (rs, row) ->
            new Milestone(rs.getString("title"), rs.getString("note"), rs.getObject("happened_on", LocalDate.class));
    private final JdbcTemplate db;

    public BatchRepository(JdbcTemplate db) { this.db = db; }

    private static RowMapper<Batch> batch(boolean owner) {
        return (rs, row) -> new Batch(rs.getLong("id"), rs.getObject("public_code", UUID.class),
                rs.getLong("product_id"), rs.getString("product_slug"), rs.getString("product_name"),
                rs.getString("workshop_slug"), rs.getString("workshop_name"), rs.getString("family_history"),
                rs.getString("code"), rs.getString("name"), rs.getString("description"),
                Kind.valueOf(rs.getString("kind")), rs.getInt("quantity"), rs.getString("unit"),
                owner || rs.getBoolean("price_visible") ? rs.getBigDecimal("reference_price") : null,
                rs.getBoolean("price_visible"), rs.getString("supply_capacity"),
                rs.getObject("available_from", LocalDate.class), Visibility.valueOf(rs.getString("visibility")),
                rs.getString("partner_note"), rs.getBoolean("hidden"));
    }

    public PageResponse<Batch> ownList(long workshop, PageQuery page) {
        return page(page, "p.workshop_id = ?", true, workshop);
    }

    public PageResponse<Batch> published(PageQuery page, String product, String workshop, Kind kind) {
        String filter = VISIBLE + " AND " + BATCH_VISIBILITY + """
                 AND (CAST(? AS TEXT) IS NULL OR p.slug = ?)
                 AND (CAST(? AS TEXT) IS NULL OR w.slug = ?)
                 AND (CAST(? AS TEXT) IS NULL OR b.kind = ?)
                """;
        String kindName = kind == null ? null : kind.name();
        return page(page, filter, false, product, product, workshop, workshop, kindName, kindName);
    }

    private PageResponse<Batch> page(PageQuery page, String filter, boolean owner, Object... arguments) {
        long count = db.queryForObject("SELECT count(*) " + FROM + " WHERE " + filter, Long.class, arguments);
        Object[] paging = java.util.Arrays.copyOf(arguments, arguments.length + 2);
        paging[arguments.length] = page.getSize();
        paging[arguments.length + 1] = (long) page.getPage() * page.getSize();
        var items = db.query(SELECT + FROM + " WHERE " + filter + " ORDER BY b.id DESC LIMIT ? OFFSET ?", batch(owner), paging);
        int pages = (int) ((count + page.getSize() - 1) / page.getSize());
        return new PageResponse<>(items, page.getPage(), page.getSize(), count, pages, page.getPage() == 0,
                page.getPage() + 1 >= pages);
    }

    public Optional<Batch> owned(long id, long workshop, boolean lock) {
        return db.query(SELECT + FROM + " WHERE b.id = ? AND p.workshop_id = ?" + (lock ? " FOR UPDATE OF b" : ""),
                batch(true), id, workshop).stream().findFirst();
    }

    public Optional<Batch> publicDetail(UUID code, String tokenHash) {
        return db.query(SELECT + FROM + " WHERE b.public_code = ? AND " + VISIBLE + " AND ("
                        + BATCH_VISIBILITY + " OR b.share_token_hash = ?)", batch(false), code, tokenHash)
                .stream().findFirst();
    }

    public long create(long workshop, UUID uuid, Input input) {
        return db.query("""
                INSERT INTO batches(product_id, public_code, code, name, description, kind, quantity, unit,
                    reference_price, price_visible, supply_capacity, available_from, visibility, partner_note)
                SELECT p.id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM products p WHERE p.id = ? AND p.workshop_id = ?
                RETURNING id
                """, (rs, row) -> rs.getLong(1), uuid, input.code().strip(), input.name().strip(), input.description(),
                input.kind().name(), input.quantity(), input.unit().strip(), input.price(), input.priceVisible(),
                input.supplyCapacity(), input.availableFrom(), input.visibility().name(), input.partnerNote(),
                input.productId(), workshop).stream().findFirst().orElse(0L);
    }

    public boolean update(long id, long workshop, Input input) {
        return db.update("""
                UPDATE batches SET product_id = p.id, code = ?, name = ?, description = ?, kind = ?, quantity = ?,
                    unit = ?, reference_price = ?, price_visible = ?, supply_capacity = ?, available_from = ?,
                    visibility = ?, partner_note = ?, updated_at = clock_timestamp(),
                    share_token_hash = CASE WHEN visibility = ? THEN share_token_hash ELSE NULL END
                FROM products p WHERE batches.id = ? AND p.id = ? AND p.workshop_id = ?
                """, input.code().strip(), input.name().strip(), input.description(), input.kind().name(),
                input.quantity(), input.unit().strip(), input.price(), input.priceVisible(), input.supplyCapacity(),
                input.availableFrom(), input.visibility().name(), input.partnerNote(), input.visibility().name(),
                id, input.productId(), workshop) > 0;
    }

    public List<Milestone> milestones(long batch) {
        return db.query("""
                SELECT title, note, happened_on FROM batch_milestones WHERE batch_id = ? ORDER BY happened_on, id
                """, MILESTONE, batch);
    }

    public void replaceMilestones(long batch, List<Milestone> items) {
        db.update("DELETE FROM batch_milestones WHERE batch_id = ?", batch);
        for (Milestone item : items) db.update("""
                INSERT INTO batch_milestones(batch_id, title, note, happened_on) VALUES (?, ?, ?, ?)
                """, batch, item.title().strip(), item.note(), item.happenedOn());
    }

    public void setTokenHash(long id, String hash) {
        db.update("UPDATE batches SET share_token_hash = ? WHERE id = ?", hash, id);
    }

    public boolean hide(long id) {
        return db.update("UPDATE batches SET hidden = true WHERE id = ?", id) > 0;
    }

    public Optional<Batch> adminDetail(long id) {
        return db.query(SELECT + FROM + " WHERE b.id = ?", batch(true), id).stream().findFirst();
    }
}
