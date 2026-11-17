package vn.vcraft.backend.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.*;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
  List<Quote> findByRequestIdOrderByIdAsc(Long requestId);

  Optional<Quote> findByRequestIdAndWorkshopId(Long requestId, Long workshopId);

  boolean existsByRequestIdAndStatus(Long requestId, QuoteStatus status);

  @Query("select q.requestId from Quote q where q.id = :id")
  Optional<Long> requestIdOf(@Param("id") long id);
}
