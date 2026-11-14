package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.FeaturedContent.*;

@Repository
public class FeaturedRepository {
    private final JdbcTemplate db;

    public FeaturedRepository(JdbcTemplate db) { this.db = db; }

    public Input selected() {
        return db.query("SELECT workshop_id, product_id, batch_id FROM featured_content WHERE id = 1",
                (rs, row) -> new Input(rs.getObject(1, Long.class), rs.getObject(2, Long.class),
                rs.getObject(3, Long.class))).stream().findFirst().orElse(new Input(null, null, null));
    }

    public boolean save(Input input) {
        return db.update("""
                WITH choices(workshop_id, product_id, batch_id) AS (VALUES (CAST(? AS BIGINT), CAST(? AS BIGINT), CAST(? AS BIGINT)))
                INSERT INTO featured_content(id, workshop_id, product_id, batch_id)
                SELECT 1, c.workshop_id, c.product_id, c.batch_id FROM choices c
                WHERE (c.workshop_id IS NULL OR EXISTS (SELECT FROM workshops w WHERE w.id = c.workshop_id AND w.approved))
                  AND (c.product_id IS NULL OR EXISTS (
                    SELECT FROM products p JOIN workshops w ON w.id = p.workshop_id
                    WHERE p.id = c.product_id AND w.approved AND NOT p.hidden AND NOT p.admin_hidden))
                  AND (c.batch_id IS NULL OR EXISTS (
                    SELECT FROM batches b JOIN products p ON p.id = b.product_id JOIN workshops w ON w.id = p.workshop_id
                    WHERE b.id = c.batch_id AND w.approved AND NOT p.hidden AND NOT p.admin_hidden AND NOT b.hidden AND b.visibility = 'PUBLIC'))
                ON CONFLICT (id) DO UPDATE SET workshop_id = excluded.workshop_id, product_id = excluded.product_id,
                    batch_id = excluded.batch_id
                """, input.workshopId(), input.productId(), input.batchId()) == 1;
    }

    public Home published() {
        return db.query("""
                SELECT w.id AS wid, w.slug AS wslug, w.name AS wname,
                    p.id AS pid, p.slug AS pslug, p.name AS pname, p.image_path,
                    b.id AS bid, b.public_code::text AS bcode, b.name AS bname
                FROM featured_content f
                LEFT JOIN workshops w ON w.id = f.workshop_id AND w.approved
                LEFT JOIN products p ON p.id = f.product_id AND NOT p.hidden AND NOT p.admin_hidden
                    AND EXISTS (SELECT FROM workshops pw WHERE pw.id = p.workshop_id AND pw.approved)
                LEFT JOIN batches b ON b.id = f.batch_id AND b.visibility = 'PUBLIC' AND NOT b.hidden
                    AND EXISTS (SELECT FROM products bp JOIN workshops bw ON bw.id = bp.workshop_id
                        WHERE bp.id = b.product_id AND bw.approved AND NOT bp.hidden AND NOT bp.admin_hidden)
                WHERE f.id = 1
                """, (rs, row) -> new Home(
                rs.getString("wslug") == null ? null : new Item(rs.getLong("wid"), rs.getString("wslug"), rs.getString("wname"), null),
                rs.getString("pslug") == null ? null : new Item(rs.getLong("pid"), rs.getString("pslug"), rs.getString("pname"), rs.getString("image_path")),
                rs.getString("bcode") == null ? null : new Item(rs.getLong("bid"), rs.getString("bcode"), rs.getString("bname"), null)))
                .stream().findFirst().orElse(new Home(null, null, null));
    }
}
