package vn.vcraft.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import vn.vcraft.backend.model.ProcessStep;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
    List<ProcessStep> findByProductIdOrderByPosition(long productId);

    @Modifying
    @Query("delete from ProcessStep s where s.product.id = :productId")
    void removeForProduct(long productId);
}
