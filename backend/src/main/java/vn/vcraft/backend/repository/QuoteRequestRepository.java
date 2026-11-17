package vn.vcraft.backend.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.QuoteRequest;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from QuoteRequest r where r.id = :id")
  Optional<QuoteRequest> lockById(@Param("id") long id);

  Page<QuoteRequest> findByBuyerAccountId(Long buyerAccountId, Pageable pageable);
}
