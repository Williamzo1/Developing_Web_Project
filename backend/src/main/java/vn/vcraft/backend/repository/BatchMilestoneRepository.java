package vn.vcraft.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.BatchMilestone;
import java.util.List;

public interface BatchMilestoneRepository extends JpaRepository<BatchMilestone, Long> {

    @Query("""
        SELECT m FROM BatchMilestone m
        WHERE m.batchId = :batchId ORDER BY m.happenedOn, m.id
        """)
    List<BatchMilestone> findByBatchId(@Param("batchId") long batchId);

    @Modifying
    @Query("DELETE FROM BatchMilestone m WHERE m.batchId = :batchId")
    void deleteByBatchId(@Param("batchId") long batchId);
}
