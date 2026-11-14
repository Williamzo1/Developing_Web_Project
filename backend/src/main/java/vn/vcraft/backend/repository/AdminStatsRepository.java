package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.AdminStats;

@Repository
public class AdminStatsRepository {
    private final JdbcTemplate db;

    public AdminStatsRepository(JdbcTemplate db) { this.db = db; }

    public AdminStats totals() {
        return db.queryForObject("""
                SELECT (SELECT count(*) FROM workshops) AS workshops,
                       (SELECT count(*) FROM products) AS products,
                       (SELECT count(*) FROM batches) AS batches,
                       (SELECT count(*) FROM quote_requests) AS quote_requests
                """, (rs, row) -> new AdminStats(rs.getLong("workshops"), rs.getLong("products"),
                rs.getLong("batches"), rs.getLong("quote_requests")));
    }
}
