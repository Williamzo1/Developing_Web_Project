package vn.vcraft.backend.controller;

import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
    private final JdbcTemplate jdbc;

    HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/api/health/db")
    ResponseEntity<Map<String, String>> databaseHealth() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (DataAccessException exception) {
            throw new vn.vcraft.backend.exception.ApiException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "DATABASE_UNAVAILABLE", "Chưa kết nối được cơ sở dữ liệu.");
        }
    }
}
