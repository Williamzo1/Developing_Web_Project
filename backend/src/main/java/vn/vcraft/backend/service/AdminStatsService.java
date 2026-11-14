package vn.vcraft.backend.service;

import org.springframework.stereotype.Service;
import vn.vcraft.backend.dto.AdminStats;
import vn.vcraft.backend.repository.AdminStatsRepository;

@Service
public class AdminStatsService {
    private final AdminStatsRepository stats;

    public AdminStatsService(AdminStatsRepository stats) { this.stats = stats; }

    public AdminStats totals() { return stats.totals(); }
}
