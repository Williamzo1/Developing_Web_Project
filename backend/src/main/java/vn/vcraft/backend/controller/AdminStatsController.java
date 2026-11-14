package vn.vcraft.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vcraft.backend.dto.AdminStats;
import vn.vcraft.backend.service.AdminStatsService;

@RestController
class AdminStatsController {
    private final AdminStatsService stats;

    AdminStatsController(AdminStatsService stats) { this.stats = stats; }

    @GetMapping("/api/admin/stats")
    AdminStats totals() { return stats.totals(); }
}
