package vn.vcraft.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.ProcessStep;
import java.util.List;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {

    @Query("SELECT s FROM ProcessStep s WHERE s.productId = :pid ORDER BY s.position")
    List<ProcessStep> findByProductId(@Param("pid") long productId);

    @Modifying
    @Query("DELETE FROM ProcessStep s WHERE s.productId = :pid")
    void deleteByProductId(@Param("pid") long productId);
}
