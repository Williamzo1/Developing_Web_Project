package vn.vcraft.backend.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.NewsData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;

@Repository
public class NewsRepository {
    private static final RowMapper<News> NEWS = (rs, row) -> new News(rs.getLong("id"), rs.getString("slug"),
            rs.getString("title"), rs.getString("body"), Kind.valueOf(rs.getString("kind")),
            rs.getObject("event_start", OffsetDateTime.class), rs.getObject("event_end", OffsetDateTime.class),
            rs.getString("event_place"), rs.getBoolean("hidden"), rs.getObject("published_at", OffsetDateTime.class));
    private final JdbcTemplate db;

    public NewsRepository(JdbcTemplate db) { this.db = db; }

    public PageResponse<Summary> list(PageQuery page, Kind kind, boolean admin) {
        String filter = " WHERE (? OR NOT hidden) AND (CAST(? AS TEXT) IS NULL OR kind = ?)";
        String kindName = kind == null ? null : kind.name();
        long count = db.queryForObject("SELECT count(*) FROM news_posts" + filter, Long.class, admin, kindName, kindName);
        var items = db.query("SELECT id, slug, title, kind, hidden, published_at FROM news_posts" + filter
                + " ORDER BY published_at DESC, id DESC LIMIT ? OFFSET ?",
                (rs, row) -> new Summary(rs.getLong("id"), rs.getString("slug"), rs.getString("title"),
                        Kind.valueOf(rs.getString("kind")), rs.getBoolean("hidden"),
                        rs.getObject("published_at", OffsetDateTime.class)),
                admin, kindName, kindName, page.getSize(), (long) page.getSize() * page.getPage());
        int pages = (int) ((count + page.getSize() - 1) / page.getSize());
        return new PageResponse<>(items, page.getPage(), page.getSize(), count, pages, page.getPage() == 0,
                page.getPage() + 1 >= pages);
    }

    public Optional<News> published(String slug) {
        return db.query("SELECT * FROM news_posts WHERE slug = ? AND NOT hidden", NEWS, slug).stream().findFirst();
    }

    public Optional<News> find(long id) {
        return db.query("SELECT * FROM news_posts WHERE id = ?", NEWS, id).stream().findFirst();
    }

    public News insert(long author, String slug, Input input) {
        return db.queryForObject("""
                INSERT INTO news_posts(author_account_id, slug, title, body, kind, event_start, event_end, event_place, hidden)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *
                """, NEWS, author, slug, input.title().strip(), input.body(), input.kind().name(),
                input.eventStart(), input.eventEnd(), input.eventPlace(), input.hidden());
    }

    public Optional<News> update(long id, Input input) {
        return db.query("""
                UPDATE news_posts SET title = ?, body = ?, kind = ?, event_start = ?, event_end = ?,
                    event_place = ?, hidden = ?, updated_at = clock_timestamp() WHERE id = ? RETURNING *
                """, NEWS, input.title().strip(), input.body(), input.kind().name(), input.eventStart(), input.eventEnd(),
                input.eventPlace(), input.hidden(), id).stream().findFirst();
    }
}
