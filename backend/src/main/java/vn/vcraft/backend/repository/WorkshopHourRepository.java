package vn.vcraft.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.vcraft.backend.dto.WorkshopHourInput;
import vn.vcraft.backend.dto.WorkshopHourResponse;

import java.sql.Time;
import java.time.*;
import java.util.List;

@Repository
public class WorkshopHourRepository {
    private final JdbcTemplate db;

    public WorkshopHourRepository(JdbcTemplate db) { this.db = db; }

    /** Thay toàn bộ giờ mở cửa của xưởng. */
    public void replaceHours(long workshopId, List<WorkshopHourInput> hours) {
        db.update("DELETE FROM workshop_hours WHERE workshop_id = ?", workshopId);
        for (var h : hours) {
            db.update("""
                INSERT INTO workshop_hours(workshop_id, day_of_week, open_time, close_time)
                VALUES (?, ?, ?::time, ?::time)
                ON CONFLICT (workshop_id, day_of_week) DO UPDATE
                    SET open_time = EXCLUDED.open_time, close_time = EXCLUDED.close_time
                """,
                    workshopId, h.dayOfWeek(), h.openTime(), h.closeTime());
        }
    }

    public List<WorkshopHourResponse> findByWorkshop(long workshopId) {
        return db.query("""
            SELECT day_of_week, open_time, close_time
            FROM workshop_hours WHERE workshop_id = ? ORDER BY day_of_week
            """,
                (rs, r) -> new WorkshopHourResponse(
                        rs.getInt("day_of_week"),
                        rs.getString("open_time"),
                        rs.getString("close_time")),
                workshopId);
    }

    /**
     * Tính trạng thái mở cửa hiện tại (UTC+7 — Hà Nội).
     * Trả null nếu hộ chưa khai báo giờ mở cửa.
     * Trả false nếu hôm nay nằm trong workshop_closures.
     */
    public Boolean isOpenNow(long workshopId) {
        ZonedDateTime nowHanoi = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate today = nowHanoi.toLocalDate();
        LocalTime nowTime = nowHanoi.toLocalTime();
        int dayOfWeek = nowHanoi.getDayOfWeek().getValue() % 7; // 0=CN, 1=T2…6=T7

        // Kiểm tra ngày đóng cửa đột xuất
        var closed = db.queryForObject("""
            SELECT COUNT(*) FROM workshop_closures
            WHERE workshop_id = ? AND closed_on = ?
            """, Integer.class, workshopId, today);
        if (closed != null && closed > 0) return false;

        // Kiểm tra giờ hôm nay
        return db.query("""
            SELECT open_time, close_time FROM workshop_hours
            WHERE workshop_id = ? AND day_of_week = ?
            """,
                (rs, r) -> {
                    Time open = rs.getTime("open_time");
                    Time close = rs.getTime("close_time");
                    LocalTime openTime = open.toLocalTime();
                    LocalTime closeTime = close.toLocalTime();
                    return nowTime.isAfter(openTime) && nowTime.isBefore(closeTime);
                },
                workshopId, dayOfWeek).stream().findFirst().orElse(null);
        // null = không có khai báo giờ cho thứ này
    }

    public void addClosure(long workshopId, LocalDate closedOn, String reason) {
        db.update("""
            INSERT INTO workshop_closures(workshop_id, closed_on, reason)
            VALUES (?, ?, ?)
            ON CONFLICT (workshop_id, closed_on) DO UPDATE SET reason = EXCLUDED.reason
            """, workshopId, closedOn, reason);
    }

    public boolean deleteClosure(long workshopId, long closureId) {
        int rows = db.update("""
            DELETE FROM workshop_closures WHERE id = ? AND workshop_id = ?
            """, closureId, workshopId);
        return rows > 0;
    }
}
