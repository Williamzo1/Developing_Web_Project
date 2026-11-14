package vn.vcraft.backend.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.QuoteData.*;

@Repository
public class QuoteRepository {
    private static final String REQUEST = """
            SELECT r.*, c.slug AS category, w.slug AS target_slug, w.name AS target_name
            FROM quote_requests r JOIN categories c ON c.id = r.category_id
            LEFT JOIN workshops w ON w.id = r.target_workshop_id
            """;
    private static final RowMapper<Request> REQUEST_ROW = (rs, row) -> new Request(
            rs.getLong("id"), rs.getLong("buyer_account_id"), rs.getString("category"), rs.getString("title"),
            rs.getInt("quantity"), rs.getString("unit"), rs.getString("spec"), rs.getObject("desired_by", LocalDate.class),
            rs.getString("note"), rs.getObject("bidding_deadline", OffsetDateTime.class),
            rs.getObject("target_workshop_id", Long.class), rs.getString("target_slug"), rs.getString("target_name"),
            State.valueOf(rs.getString("status")), rs.getObject("accepted_quote_id", Long.class), rs.getLong("version"));
    private static final String QUOTES = """
            SELECT q.*, w.name AS workshop, w.phone, w.contact_email, w.facebook_url
            FROM quotes q JOIN workshops w ON w.id = q.workshop_id WHERE q.request_id = ?
            """;
    private static final RowMapper<Quote> QUOTE_ROW = (rs, row) -> new Quote(
            rs.getLong("id"), rs.getString("workshop"), rs.getBigDecimal("unit_price"), rs.getBigDecimal("total_price"),
            rs.getInt("lead_time_days"), rs.getString("note"), rs.getString("status"),
            rs.getObject("updated_at", OffsetDateTime.class), rs.getString("phone"),
            rs.getString("contact_email"), rs.getString("facebook_url"));
    private final JdbcTemplate db;

    public QuoteRepository(JdbcTemplate db) { this.db = db; }

    public List<Request> buyerRequests(long buyer) {
        return db.query(REQUEST + " WHERE r.buyer_account_id = ? ORDER BY r.id DESC", REQUEST_ROW, buyer);
    }

    public List<Request> artisanRequests(long workshop) {
        return db.query(REQUEST + " WHERE r.target_workshop_id = ? ORDER BY r.id DESC", REQUEST_ROW, workshop);
    }

    public Optional<Request> buyerRequest(long id, long buyer, boolean lock) {
        return db.query(REQUEST + " WHERE r.id = ? AND r.buyer_account_id = ?" + (lock ? " FOR UPDATE OF r" : ""),
                REQUEST_ROW, id, buyer).stream().findFirst();
    }

    public Optional<Request> artisanRequest(long id, long workshop, boolean lock) {
        return db.query(REQUEST + " WHERE r.id = ? AND r.target_workshop_id = ?" + (lock ? " FOR UPDATE OF r" : ""),
                REQUEST_ROW, id, workshop).stream().findFirst();
    }

    public Long categoryId(String slug) {
        return db.query("SELECT id FROM categories WHERE slug = ?", (rs, row) -> rs.getLong(1), slug)
                .stream().findFirst().orElse(null);
    }

    public Long targetId(String slug) {
        return db.query("""
                SELECT w.id FROM workshops w JOIN accounts a ON a.workshop_id = w.id
                WHERE w.slug = ? AND w.approved FOR SHARE OF w
                """, (rs, row) -> rs.getLong(1), slug).stream().findFirst().orElse(null);
    }

    public long create(long buyer, long category, Draft input) {
        return db.queryForObject("""
                INSERT INTO quote_requests (buyer_account_id, category_id, title, quantity, unit, spec,
                    desired_by, note, bidding_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
                """, Long.class, buyer, category, input.title().strip(), input.quantity(), input.unit().strip(),
                input.spec(), input.desiredBy(), input.note(), input.biddingDeadline());
    }

    public void update(long id, long category, Draft input) {
        db.update("""
                UPDATE quote_requests SET category_id = ?, title = ?, quantity = ?, unit = ?, spec = ?,
                    desired_by = ?, note = ?, bidding_deadline = ?, version = version + 1, updated_at = clock_timestamp()
                WHERE id = ?
                """, category, input.title().strip(), input.quantity(), input.unit().strip(), input.spec(),
                input.desiredBy(), input.note(), input.biddingDeadline(), id);
    }

    public void delete(long id) { db.update("DELETE FROM quote_requests WHERE id = ?", id); }

    public void transition(long id, State status, Long target, Long accepted) {
        db.update("""
                UPDATE quote_requests SET status = ?, target_workshop_id = ?, accepted_quote_id = ?,
                    version = version + 1, updated_at = clock_timestamp() WHERE id = ?
                """, status.name(), target, accepted, id);
    }

    public void event(long request, long actor, State from, State to) {
        db.update("INSERT INTO quote_request_events(request_id, actor_account_id, from_status, to_status) VALUES (?, ?, ?, ?)",
                request, actor, from == null ? null : from.name(), to.name());
    }

    public List<Event> events(long id) {
        return db.query("""
                SELECT e.from_status, e.to_status, e.created_at, a.role
                FROM quote_request_events e JOIN accounts a ON e.actor_account_id = a.id
                WHERE e.request_id = ? ORDER BY e.id
                """, (rs, row) -> new Event(rs.getString("from_status") == null ? null : State.valueOf(rs.getString("from_status")),
                State.valueOf(rs.getString("to_status")), rs.getObject("created_at", OffsetDateTime.class), rs.getString("role")), id);
    }

    public List<Quote> quotes(long id) {
        return db.query(QUOTES + " ORDER BY q.id", QUOTE_ROW, id);
    }

    public List<Quote> ownQuote(long id, long workshop) {
        return db.query(QUOTES + " AND q.workshop_id = ?", QUOTE_ROW, id, workshop);
    }

    public void submit(long id, long workshop, Offer input) {
        db.update("""
                INSERT INTO quotes(request_id, workshop_id, unit_price, total_price, lead_time_days, note, status)
                VALUES (?, ?, ?, ?, ?, ?, 'SUBMITTED')
                ON CONFLICT(request_id, workshop_id) DO UPDATE SET unit_price = excluded.unit_price,
                    total_price = excluded.total_price, lead_time_days = excluded.lead_time_days,
                    note = excluded.note, updated_at = greatest(clock_timestamp(), quotes.updated_at + interval '1 microsecond')
                """, id, workshop, input.unitPrice(), input.totalPrice(), input.leadTimeDays(), input.note());
    }

    public void closeQuotes(long id, Long accepted) {
        db.update("""
                UPDATE quotes SET status = CASE WHEN id = CAST(? AS BIGINT) THEN 'ACCEPTED' ELSE 'NOT_SELECTED' END,
                    updated_at = clock_timestamp() WHERE request_id = ? AND status = 'SUBMITTED'
                """, accepted, id);
    }
}
